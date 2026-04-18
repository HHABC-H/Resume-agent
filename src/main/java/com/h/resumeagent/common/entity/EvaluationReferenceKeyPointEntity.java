package com.h.resumeagent.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evaluation_reference_key_point", indexes = @Index(name = "idx_eval_ref_key_point_ref", columnList = "reference_answer_id"))
public class EvaluationReferenceKeyPointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reference_answer_id", nullable = false)
    private EvaluationReferenceAnswerEntity referenceAnswer;

    @Column(name = "key_point", nullable = false, length = 1000)
    private String keyPoint;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
