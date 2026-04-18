package com.h.resumeagent.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SseEmitterUtil {

    private static final Logger logger = LoggerFactory.getLogger(SseEmitterUtil.class);
    private static final long SSE_TIMEOUT_MILLIS = 600_000L;

    public static EmitterState createEmitterState() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        EmitterState state = new EmitterState(emitter);
        emitter.onCompletion(() -> state.completed.set(true));
        emitter.onTimeout(() -> {
            if (state.completed.compareAndSet(false, true)) {
                logger.warn("SSE timeout after {} ms", SSE_TIMEOUT_MILLIS);
                try {
                    emitter.complete();
                } catch (IllegalStateException ignored) {
                }
            }
        });
        emitter.onError(ex -> state.completed.set(true));
        return state;
    }

    public static boolean sendEvent(EmitterState state, String event, Object data) {
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

    public static void completeEmitter(EmitterState state) {
        if (state.completed.compareAndSet(false, true)) {
            try {
                state.emitter.complete();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public static void completeEmitterWithError(EmitterState state, Exception ex) {
        if (state.completed.compareAndSet(false, true)) {
            try {
                state.emitter.completeWithError(ex);
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public static class EmitterState {
        public final SseEmitter emitter;
        public final AtomicBoolean completed = new AtomicBoolean(false);

        public EmitterState(SseEmitter emitter) {
            this.emitter = emitter;
        }
    }
}
