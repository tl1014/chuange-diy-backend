package com.chuange.diy.backend.model.db;

import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 60)
    public String name;

    @Column(nullable = false, unique = true, length = 60)
    public String code;

    @Column(name = "sort_no", nullable = false)
    public Integer sortNo = 0;

    @Column(nullable = false)
    public Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public Category parent;
}