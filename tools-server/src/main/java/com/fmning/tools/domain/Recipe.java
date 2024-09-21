package com.fmning.tools.domain;

import com.fmning.tools.type.RecipeCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@Table(name="tl_recipes")
@DynamicUpdate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    @Column(name="name")
    private String name;

    @Column(name="category")
    @Enumerated(EnumType.STRING)
    private RecipeCategory category;

    @Column(name="content")
    private String content;

    @Column(name="thumbnail")
    private String thumbnail;

    @Column(name="created")
    private Instant created;
}
