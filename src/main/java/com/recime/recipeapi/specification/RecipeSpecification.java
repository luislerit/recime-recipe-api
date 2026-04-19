package com.recime.recipeapi.specification;

import com.recime.recipeapi.entity.Ingredient;
import com.recime.recipeapi.entity.Instruction;
import com.recime.recipeapi.entity.Recipe;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;
import java.util.UUID;

public class RecipeSpecification {

    private RecipeSpecification() {}

    public static Specification<Recipe> byUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Recipe> byVegetarian(Boolean isVegetarian) {
        if (isVegetarian == null) return null;
        return (root, query, cb) -> cb.equal(root.get("isVegetarian"), isVegetarian);
    }

    public static Specification<Recipe> byServings(Integer servings) {
        if (servings == null) return null;
        return (root, query, cb) -> cb.equal(root.get("servings"), servings);
    }

    public static Specification<Recipe> byIncludeIngredients(Set<String> include) {
        if (include == null || include.isEmpty()) return null;

        return (root, query, cb) -> {
            jakarta.persistence.criteria.Predicate[] predicates = include.stream()
                    .map(name -> {
                        Subquery<Long> sub = query.subquery(Long.class);
                        Root<Ingredient> ingRoot = sub.from(Ingredient.class);
                        sub.select(ingRoot.get("id"))
                                .where(
                                        cb.equal(ingRoot.get("recipe"), root),
                                        cb.equal(cb.lower(ingRoot.get("name")), name.toLowerCase())
                                );
                        return cb.exists(sub);
                    })
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.and(predicates);
        };
    }

    public static Specification<Recipe> byExcludeIngredients(Set<String> exclude) {
        if (exclude == null || exclude.isEmpty()) return null;

        return (root, query, cb) -> {
            jakarta.persistence.criteria.Predicate[] predicates = exclude.stream()
                    .map(name -> {
                        Subquery<Long> sub = query.subquery(Long.class);
                        Root<Ingredient> ingRoot = sub.from(Ingredient.class);
                        sub.select(ingRoot.get("id"))
                                .where(
                                        cb.equal(ingRoot.get("recipe"), root),
                                        cb.equal(cb.lower(ingRoot.get("name")), name.toLowerCase())
                                );
                        return cb.not(cb.exists(sub));
                    })
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.and(predicates);
        };
    }

    public static Specification<Recipe> byInstructionKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Instruction> instrRoot = sub.from(Instruction.class);
            sub.select(instrRoot.get("id"))
                    .where(
                            cb.equal(instrRoot.get("recipe"), root),
                            cb.like(cb.lower(instrRoot.get("description")), "%" + keyword.toLowerCase() + "%")
                    );
            return cb.exists(sub);
        };
    }
}
