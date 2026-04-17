package com.h.resumeagent.controller;

import com.h.resumeagent.auth.AuthService;
import com.h.resumeagent.auth.AuthTokenInterceptor;
import com.h.resumeagent.common.dto.InterviewEvaluation;
import com.h.resumeagent.common.dto.InterviewFollowUpRequest;
import com.h.resumeagent.common.dto.InterviewFollowUpResponse;
import com.h.resumeagent.common.dto.InterviewQuestions;
import com.h.resumeagent.common.dto.ResumeData;
import com.h.resumeagent.common.dto.ResumeScoreResult;
import jakarta.servlet.http.Cookie;
import com.h.resumeagent.service.MockInterviewService;
import com.h.resumeagent.utils.ResumeDocumentUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class MockInterviewController {

    private static final Logger logger = LoggerFactory.getLogger(MockInterviewController.class);
    private static final String TOKEN_COOKIE_NAME = "RA_TOKEN";
    private static final long SSE_TIMEOUT_MILLIS = 600_000L;

    private final MockInterviewService interviewService;
    private final AuthService authService;
    private final ResumeDocumentUtils resumeDocumentUtils;
    private final AsyncTaskExecutor sseTaskExecutor;

    public MockInterviewController(
            MockInterviewService interviewService,
            AuthService authService,
            ResumeDocumentUtils resumeDocumentUtils,
            @Qualifier("sseTaskExecutor")
            AsyncTaskExecutor sseTaskExecutor) {
        this.interviewService = interviewService;
        this.authService = authService;
        this.resumeDocumentUtils = resumeDocumentUtils;
        this.sseTaskExecutor = sseTaskExecutor;
    }

    /**
     * 濠电姷顣藉Σ鍛村磻閹捐泛绶ゅΔ锝呭暞閸嬪鏌ｅΟ娆惧殭鏉?
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    /**
     * 濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸劍閺呮繈鏌ㄥ┑鍡樺妞ゎ偅娲熼弻娑㈠箻閼艰泛鍘℃繛鎴炴尭缁夊綊寮诲鍫闂佸憡鎸鹃崰搴ㄦ偩閻戣棄顫呴柕鍫濇噹閼板灝鈹戦悙鏉戠仸闁绘鍨垮畷鎴﹀箻閺夋垹绐為梺褰掑亰閸欏骸螞?
     */
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    /**
     * 闂傚倸鍊风粈渚€骞夐敓鐘虫櫔婵犵妲呴崑鍛淬€冩径鎰﹂柟鐗堟緲缁€鍫澝归敐鍥ㄥ殌妞ゅ孩鐩娲传閸曞灚歇濠电偛顦板ú婊呭垝閺冨牆鍨傛い鏇炴噺鐎靛矂姊洪棃娑氬婵炶绠撳鎶筋敆娴ｈ櫣顔?
     */
    @GetMapping("/history")
    public String historyPage() {
        return "history";
    }

    @GetMapping("/interview-entry")
    public String interviewEntryPage() {
        return "interview-entry";
    }

    /**
     * 濠电姷鏁搁崑鐐哄垂閸洖绠伴柟闂寸劍閺呮繈鏌ㄥ┑鍡樺妞ゎ偅娲熼弻娑㈠箻閼艰泛鍘℃繛鎴炴尭缁夊綊寮诲鍫闂佸憡鎸鹃崰搴ㄦ偩閻戣棄顫呴柕鍫濇噹閼板灝鈹戦埥鍡楃仴鐎规洜鏁婚崺鈧い鎺戭槸閺嬫盯妫佹径鎰厽婵☆垳鍘ч崝瀣棯椤撱垻鐣洪柡灞诲€濋幊锟犲Χ閸涱喚浜栭梻浣哥秺椤ユ挻绻涢埀顒傗偓瑙勬礈閸犳牠銆侀弴銏狀潊闁冲灈鏅涙禍?
     */
    @PostMapping("/api/resume/upload")
    @ResponseBody
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "positionType", required = false, defaultValue = MockInterviewService.POSITION_BACKEND_JAVA) String positionType,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
            }


            // 婵犵數濮烽。钘壩ｉ崨鏉戠；闁逞屽墴閺屾稓鈧綆鍋呭畷宀勬煛瀹€瀣？濞寸媴濡囬幏鐘诲箵閹烘埈娼ラ梻鍌欑閹碱偊顢栭崨鏉戠柈妞ゆ牗绮庨惌澶愭煙閻戞﹩娈旂紒鈧崼婢濆綊鏁愰崶鈺傛啒濠电偟鍘ч悥鐓庮潖?
            String fileName = file.getOriginalFilename();
            if (!resumeDocumentUtils.isSupportedResumeFile(fileName)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF/DOC/DOCX/TXT is supported"));
            }

            // 闂傚倷娴囧畷鍨叏閺夋嚚娲煛閸滀焦鏅悷婊勫灴婵＄敻骞囬弶璺ㄥ€炲銈嗗笒閸婂摜绮婇敃鍌涒拺閻犲洠鈧磭浠繝銏㈡嚀濡稓鍒掓繝姘摕闁靛濡囬崢閬嶆⒑闂堟冻绱￠柛鎰屽嫬鐓曢梻鍌欑閹诧繝宕瑰畷鍥у灊婵炲棗娴氶崵鏇熴亜閹烘垵顏╅柣鎰躬閺屾洘绻濊箛娑欘€嶉柣搴㈢婵炲﹤顫忓ú顏勭閹艰揪绲哄Σ鍫ユ⒑缂佹﹩娈旀俊顐ｇ箞閵?PDF 闂傚倷娴囬褏鈧稈鏅濈划娆撳箳濡も偓妗呴梺缁樺姉閺佸摜绮堟繝鍥ㄥ€甸柨婵嗛閺嬫稓绱掗悩顔煎姕闁靛洤瀚伴獮鍥礈娴ｇ鐓傚┑鐐差嚟閸樠兠归悜钘夌劦妞ゆ帊绶￠崯蹇涙煕閻樺磭澧电€规洘鍔欓獮鏍ㄦ媴閻熼鍑介梻浣稿閸嬪懎煤閺嶎偄顥氱紓浣股戦崣蹇旀叏濡も偓濡绂嶉悙娣簻闊洦宀搁崫铏圭磼缂佹娲寸€规洟浜堕、姗€鎮╅悽鍨潓闂傚倷绶氬鑽ゅ緤娴犲绠烘繝濠傜墕缁狀垶鏌ｅΟ娆惧殭闁告娅曢妵鍕疀閹炬惌妫ゅ?
            String resumeText = resumeDocumentUtils.extractResumeText(file);

            // 闂傚倷娴囧畷鍨叏閹绢噮鏁勯柛娑欐綑閻ゎ喖霉閸忓吋缍戦柡?AI 闂傚倸鍊风粈渚€骞栭锔藉亱闁糕剝鐟ч惌鎾绘倵濞戞鎴﹀矗韫囨稒鐓熼柡鍌涘閸熺偛霉濠у灝鈧牜鎹㈠☉銏犵闁绘劗鏁搁悡鐘绘⒑鐞涒€充壕婵炲濮撮鍡涘磻閿濆鐓曢柕澶嬪灥閸犳碍绂掓總鍛娾拺?
            ResumeScoreResult scoreResult = interviewService.scoreResume(resumeText);
            
            // 濠电姷鏁搁崕鎴犲緤閽樺娲晜閻愵剙搴婇梺绋跨灱閸嬬偤宕戦妶澶嬬厪濠电倯鍐╁櫢缂佹顦靛娲焻閻愯尪瀚板褍顕埀顒冾潐濞插繘宕归挊澶屾殾闁告鍊ｉ悢鍏煎殥闁靛牆鍊告禍鐐亜閺囨浜鹃梺鍝勭焿缁绘繂鐣锋總鍓叉晝闁靛繒濯濠氭⒒娴ｈ櫣銆婇柡鍛洴瀵敻顢楅崒妤€浜鹃柣銏ゆ涧鐢埖銇勯锝囩畵闁伙絿鍏樺畷鍫曞Ω閵忥紕浜栭梻鍌氬€烽悞锔锯偓绗涘懐鐭欓柟杈鹃檮閸嬪鐓崶銊р姇闁稿鏅滅换婵嬫濞戝崬鍓扮紓浣插亾鐎光偓閸曨剛鍘搁悗瑙勬惄閸犳牠骞婅箛鎿冪唵闁哄稁鍘介埛鎴犵磽娴ｈ鐒介柣顓烇躬楠炴牜鈧稒顭囬惌灞句繆閸欏濮囬柍瑙勫灴瀹曠厧顫濋鍨棜闂備線娼荤€靛矂宕㈤崗鑲╊浄閺夊牄鍔嬬换鍡欑磽娴ｉ潧鐏╅柣蹇ョ節閺岀喖顢涘☉娆樻閻庢鍣崳锝呯暦閸撲焦宕夐柣鎴烆焽閺嗩垶姊?session闂?
            String resumeId = UUID.randomUUID().toString();
            String normalizedPositionType = interviewService.normalizePositionType(positionType);
            interviewService.saveResume(resumeId, resumeText, scoreResult, userId, normalizedPositionType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("resumeId", resumeId);
            response.put("positionType", normalizedPositionType);
            response.put("positionTypeLabel", interviewService.displayPositionType(normalizedPositionType));
            response.put("scoreResult", scoreResult);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Resume upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("缂傚倸鍊搁崐鐑芥嚄閼稿灚鍙忔い鎾卞灩绾惧鏌熼崜褏甯涢柣鎾存礃娣囧﹪顢涘鍐ㄥ濡炪們鍎荤槐鏇㈠箖椤斿皷鏋庨柟鎯ь嚟閸樻悂鏌ｆ惔顖滅У闁稿瀚幈銊╁炊椤掍胶鍘梺鎼炲劘閸斿矂鎮橀埡鍛厵? {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Scoring failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Scoring failed: " + e.getMessage()));
        }
    }

    /**
     * 缂傚倸鍊搁崐鐑芥嚄閼稿灚鍙忔い鎾卞灩绾惧鏌熼崜褏甯涢柣鎾存礃娣囧﹪顢涘鍐ㄥ濡炪們鍎茬划鎾诲蓟濞戙垹惟闁靛鏅涘浼存倵濞堝灝鏋熼柟顔煎€块悰顕€宕堕澶嬫櫓闂佺粯鍔﹂崜娆撴儍妤ｅ啯鈷掑ù锝呮啞閹牊銇勯幋婵囶棤闁告帗甯￠獮妯兼嫚閸欏倶鍎遍…鍧楁嚋闂堟稑顫嶅?
     */
    @GetMapping("/analysis/{resumeId}")
    public String analysisPage(@PathVariable String resumeId, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return "redirect:/login";
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return "redirect:/upload";
        }
        
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("scoreResult", resumeData.getScoreResult());
        return "analysis";
    }

    /**
     * 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ閵忊剝鐝栨繛鎴炴尭缁夊綊寮诲鍫闂佸憡鎸鹃崰搴ㄦ偩閻戣棄顫呴柕鍫濇噹閼板灝鈹戞幊閸婃劙宕戦幘缁樼厽閹肩补妲呴悡濂告煛瀹€瀣？闁逞屽墾缂嶅棝宕戦崟顒佸弿閻忕偘鍕樻禍婊堟煛閸モ晛鏋旀繛鎻掔摠椤?
     */
    @GetMapping("/api/resume/{resumeId}/analysis")
    @ResponseBody
    public ResponseEntity<?> getResumeAnalysis(@PathVariable String resumeId, HttpServletRequest request) {
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resumeData.getScoreResult());
    }

    /**
     * 闂傚倸鍊风粈渚€宕ョ€ｎ喖纾块柟鎯版鎼村﹪鏌ら懝鎵牚濞存粌缍婇弻娑㈠Ψ椤旂厧顫╅柣搴㈣壘椤︾敻寮诲鍫闂佸憡鎸婚懝楣冣€﹂崶銊х瘈婵﹩鍓涢鍝勨攽閻樼粯娑ф俊顐ｎ殔椤洭骞嬮敂瑙ｆ嫼闂佸憡绋戦敃锕傚箠閸℃稒鍊堕煫鍥风导闁垱銇勯姀锛勬噰鐎规洖鐖奸、鏃堝磼濞嗘劗娼?
     */
    @GetMapping("/api/resume/history")
    @ResponseBody
    public ResponseEntity<?> getHistory(@RequestParam(defaultValue = "20") int limit, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(interviewService.getRecentResumeHistory(userId, limit));
    }

    /**
     * 婵犵數濮烽。钘壩ｉ崨鏉戝瀭妞ゅ繐鐗嗙粈鍫熺節闂堟稓澧愰柛瀣尭椤繈顢橀悩鍐叉珰婵犵妲呴崑鍕疮鐎涙ɑ鍙忛柍褜鍓熼弻銊モ攽閸℃ê顦╃紒妤佸灥閳规垿鏁嶉崟顐℃澀闂佺锕ラ〃濠傤嚕椤愩倖瀚氱€瑰壊鍠氶崝?
     */
    @GetMapping("/interview/{resumeId}")
    public String interviewPage(@PathVariable String resumeId, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return "redirect:/login";
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return "redirect:/upload";
        }
        
        model.addAttribute("resumeId", resumeId);
        return "interview";
    }

    /**
     * 闂傚倸鍊烽悞锕傛儑瑜版帒鍨傚┑鐘宠壘缁愭鏌熼悧鍫熺凡闁搞劌鍊归幈銊ノ熼崹顔惧帿濡炪們鍎遍悧鎾诲蓟閺囷紕鐤€闁靛／鍕还婵犵鈧啿绾ч柟顔煎€搁～蹇旂節濮橆剛锛滈梺闈涚墕閹冲繘鎮靛┑鍡╂富?
     */
    @PostMapping("/api/interview/{resumeId}/questions")
    @ResponseBody
    public ResponseEntity<?> generateQuestions(@PathVariable String resumeId, HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }
            
            InterviewQuestions questions = interviewService.generateInterviewQuestions(
                    resumeData.getResumeText(),
                    resumeData.getPositionType()
            );
            interviewService.saveQuestions(resumeId, questions);
            
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Generate interview questions failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Generate interview questions failed: " + e.getMessage()));
        }
    }

    @PostMapping("/api/interview/{resumeId}/follow-up")
    @ResponseBody
    public ResponseEntity<?> generateFollowUpQuestion(
            @PathVariable String resumeId,
            @RequestBody InterviewFollowUpRequest followUpRequest,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }

            if (followUpRequest == null || followUpRequest.getQuestionIndex() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "questionIndex is required"));
            }
            if (StringUtils.isBlank(followUpRequest.getAnswer())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please answer current question before follow-up"));
            }
            if (resumeData.getQuestions() == null || resumeData.getQuestions().getQuestions() == null
                    || resumeData.getQuestions().getQuestions().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please generate interview questions first"));
            }

            int questionIndex = followUpRequest.getQuestionIndex();
            if (questionIndex < 0 || questionIndex >= resumeData.getQuestions().getQuestions().size()) {
                return ResponseEntity.badRequest().body(Map.of("error", "questionIndex out of range"));
            }

            InterviewQuestions.Question currentQuestion = resumeData.getQuestions().getQuestions().get(questionIndex);
            String followUpQuestion = interviewService.generateFollowUpQuestion(
                    resumeData.getResumeText(),
                    resumeData.getPositionType(),
                    currentQuestion,
                    followUpRequest.getAnswer()
            );

            return ResponseEntity.ok(InterviewFollowUpResponse.builder()
                    .questionIndex(questionIndex)
                    .followUpQuestion(followUpQuestion)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Generate follow-up failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Generate follow-up failed: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/api/interview/{resumeId}/questions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter generateQuestionsStream(@PathVariable String resumeId, HttpServletRequest request) {
        EmitterState emitterState = createEmitterState();
        SseEmitter emitter = emitterState.emitter;
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            sendEvent(emitterState, "error", Map.of("message", "Session not found or access denied"));
            completeEmitter(emitterState);
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (!sendEvent(emitterState, "progress", Map.of("stage", "start", "message", "婵犵數濮甸鏍窗濡ゅ啯宕查柟閭﹀枛缁躲倝鏌﹀Ο渚闁肩増瀵ч妵鍕疀閹捐泛顤€闂佺顑冮崝鎴﹀蓟閿濆绫嶉柛灞诲€楅崝鎼佹⒑鐠囪尙绠版繛宸幖椤繑绻濆顒勫敹濠电偞鍨堕…鍥ㄥ閸ヮ剚鈷戝ù鍏肩懃閻︽粓鏌涢妸銊︾【妞ゎ偄绻戠换婵嗩潩閸忓吋娅旈梻渚€鈧偛鑻晶鎾煏?.."))) {
                    return;
                }
                InterviewQuestions questions = interviewService.generateInterviewQuestions(
                        resumeData.getResumeText(),
                        resumeData.getPositionType()
                );
                if (!sendEvent(emitterState, "progress", Map.of("stage", "saving", "message", "婵犵數濮甸鏍窗濡ゅ啯宕查柟閭﹀枛缁躲倝鏌﹀Ο渚闁肩増瀵ч妵鍕疀閹炬潙娅ч梺宕囨嚀缁夌數鎹㈠┑瀣棃婵炴垶鐟Λ銈夋⒑閸︻厽娅曞┑鐐╁亾濠殿喖锕ら…宄扮暦閹烘围闁告侗鍠氳ぐ搴繆?.."))) {
                    return;
                }
                interviewService.saveQuestions(resumeId, questions);
                if (!sendEvent(emitterState, "result", questions)) {
                    return;
                }
                sendEvent(emitterState, "done", Map.of("message", "Interview questions generated"));
                completeEmitter(emitterState);
            } catch (Exception e) {
                logger.error("Stream generate interview questions failed", e);
                sendEvent(emitterState, "error", Map.of("message", "Generate interview questions failed: " + e.getMessage()));
                completeEmitterWithError(emitterState, e);
            }
        }, sseTaskExecutor);
        return emitter;
    }

    /**
     * 闂傚倸鍊风粈浣革耿鏉堚晛鍨濇い鏍仜缁€澶愭煙閻戞ê鐒炬繛灏栨櫊閺屻劑寮崹顔规寖婵＄偠顫夋繛濠囧蓟閿熺姴绀冮柨婵嗘噸婢规洘淇婇悙顏勨偓鎴﹀垂閸︻厾鐭撶憸鐗堝笚閸嬫ɑ銇勯弴妤€浜惧Δ鐘靛仜濡繈鐛澶樻晩缂佹稑顑嗙紞妯荤節?
     */
    @PostMapping("/api/interview/{resumeId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitAnswers(
            @PathVariable String resumeId,
            @RequestBody Map<Integer, String> answers,
            HttpServletRequest request) {
        try {
            Long userId = currentUserId(request);
            ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
            if (resumeData == null) {
                return ResponseEntity.notFound().build();
            }
            
            InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                resumeData.getResumeText(), 
                resumeData.getPositionType(),
                resumeData.getQuestions(), 
                answers
            );
            interviewService.saveEvaluation(resumeId, evaluation);
            
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            logger.error("Evaluate answers failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Evaluate failed: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/api/interview/{resumeId}/submit/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter submitAnswersStream(
            @PathVariable String resumeId,
            @RequestBody Map<Integer, String> answers,
            HttpServletRequest request) {
        EmitterState emitterState = createEmitterState();
        SseEmitter emitter = emitterState.emitter;
        Long userId = currentUserId(request);
        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            sendEvent(emitterState, "error", Map.of("message", "Session not found or access denied"));
            completeEmitter(emitterState);
            return emitter;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (!sendEvent(emitterState, "progress", Map.of("stage", "start", "message", "婵犵數濮甸鏍窗濡ゅ啯宕查柟閭﹀枛缁躲倝鏌﹀Ο渚闁肩増瀵ч妵鍕疀閹炬惌妫￠柣搴㈢瀹€鎼佸蓟濞戙垹鍗抽柕濞垮€楅惄搴ㄦ倵濞堝灝鏋ら柛蹇旓耿瀵鈽夐姀鐘栥劎鎲稿鍡欘浄鐟滄棃寮?.."))) {
                    return;
                }
                InterviewEvaluation evaluation = interviewService.evaluateAnswers(
                        resumeData.getResumeText(),
                        resumeData.getPositionType(),
                        resumeData.getQuestions(),
                        answers
                );
                if (!sendEvent(emitterState, "progress", Map.of("stage", "saving", "message", "婵犵數濮甸鏍窗濡ゅ啯宕查柟閭﹀枛缁躲倝鏌﹀Ο渚闁肩増瀵ч妵鍕疀閹炬潙娅ч梺宕囨嚀缁夌數鎹㈠┑瀣棃婵炴垶鐟Λ銈夋⒑閸︻厽娅曞┑鐐╁亾闂佽鍠栫紞濠傜暦閹偊妲诲銈呮櫙閸ャ劎鍘卞┑鐐叉妤犲繘宕濋悢铏圭＜閺夊牄鍔屽ù顔筋殽閻愭惌鐒介柍褜鍓涢弫濠氬疾閺屻儱绀?.."))) {
                    return;
                }
                interviewService.saveEvaluation(resumeId, evaluation);
                if (!sendEvent(emitterState, "result", evaluation)) {
                    return;
                }
                sendEvent(emitterState, "done", Map.of("message", "Interview evaluation completed"));
                completeEmitter(emitterState);
            } catch (Exception e) {
                logger.error("Stream evaluate answers failed", e);
                sendEvent(emitterState, "error", Map.of("message", "Evaluate failed: " + e.getMessage()));
                completeEmitterWithError(emitterState, e);
            }
        }, sseTaskExecutor);
        return emitter;
    }

    /**
     * 闂傚倸鍊风粈渚€骞栭銈嗗仏妞ゆ劧绠戠壕鍧楁煕閹邦垼鍤嬮柤鏉挎健閺屾稑鈽夐崡鐐典画闁诲孩纰嶅畝鎼佸蓟濞戙垹鍗抽柕濞垮€楅惄搴ㄦ倵濞堝灝娅嶆い銉︽尵濡叉劙骞樼€涙ê顎撻柣鐔哥懃鐎氼剟妫勫澶嬧拺閻犲洩灏欑粻鐗堛亜閿斿灝宓嗗┑锛勬暬瀹曠喖顢涘☉妯圭紦闂備礁鎲＄粙鎴︽晝閵夆晛违?
     */
    @GetMapping("/result/{resumeId}")
    public String resultPage(@PathVariable String resumeId, Model model, HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return "redirect:/login";
        }

        ResumeData resumeData = interviewService.getResumeById(resumeId, userId);
        if (resumeData == null) {
            return "redirect:/upload";
        }
        
        model.addAttribute("resumeId", resumeId);
        model.addAttribute("evaluation", resumeData.getEvaluation());
        model.addAttribute("questions", resumeData.getQuestions());
        return "result";
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute(AuthTokenInterceptor.CURRENT_USER_ID_ATTR);
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        String token = resolveToken(request);
        return authService.authenticate(token).map(user -> user.id()).orElse(null);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private EmitterState createEmitterState() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        EmitterState state = new EmitterState(emitter);
        emitter.onCompletion(() -> state.completed.set(true));
        emitter.onTimeout(() -> {
            if (state.completed.compareAndSet(false, true)) {
                logger.warn("SSE 闂傚倷娴囧畷鍨叏閺夋嚚娲敇閵忕姷鍝楅梻渚囧墮缁夌敻宕曢幋锔界厵婵炲牆鐏濋弸鐔兼煙椤栨ê鍔﹂柡灞剧☉閳藉宕￠悙瀵镐憾婵? {} ms", SSE_TIMEOUT_MILLIS);
                try {
                    emitter.complete();
                } catch (IllegalStateException ignored) {
                }
            }
        });
        emitter.onError(ex -> state.completed.set(true));
        return state;
    }

    private boolean sendEvent(EmitterState state, String event, Object data) {
        if (state.completed.get()) {
            return false;
        }
        try {
            state.emitter.send(SseEmitter.event().name(event).data(data));
            return true;
        } catch (IOException | IllegalStateException ignored) {
            state.completed.set(true);
            return false;
        }
    }

    private void completeEmitter(EmitterState state) {
        if (state.completed.compareAndSet(false, true)) {
            try {
                state.emitter.complete();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    private void completeEmitterWithError(EmitterState state, Exception ex) {
        if (state.completed.compareAndSet(false, true)) {
            try {
                state.emitter.completeWithError(ex);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    private static class EmitterState {
        private final SseEmitter emitter;
        private final AtomicBoolean completed = new AtomicBoolean(false);

        private EmitterState(SseEmitter emitter) {
            this.emitter = emitter;
        }
    }

}
