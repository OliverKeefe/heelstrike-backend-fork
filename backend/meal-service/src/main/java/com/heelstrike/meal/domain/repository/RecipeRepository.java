package com.heelstrike.meal.domain.repository;

import com.heelstrike.meal.domain.dto.*;
import com.heelstrike.meal.domain.entity.RecipeEntity;
import com.heelstrike.meal.util.DurationConverter;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;


@ApplicationScoped
public class RecipeRepository implements PanacheRepository<RecipeEntity> {

    public Optional<RecipeEntity> findById(long id) {
        return find("id", id).firstResultOptional();
    }

    public List<RecipeEntity> findRecipesByRequirements(RecipeRequirementsDTO requirements) {
        String query = "SELECT DISTINCT r FROM RecipeEntity r " +
                "LEFT JOIN FETCH r.macroIngredients m " +
                "LEFT JOIN FETCH m.microIngredients mi " +
                "LEFT JOIN FETCH mi.nutrients n " +
                "LEFT JOIN FETCH r.dietarySuitability dts " +
                "LEFT JOIN FETCH mi.allergens a " +
                "WHERE 1=1";

        Map<String, Object> params = new HashMap<>();

        if (requirements.getAllergensToAvoid() != null && !requirements.getAllergensToAvoid().isEmpty()) {
            query += "AND COALESCE(a.name, '') NOT IN (:allergens) ";
            params.put("allergens",
                    requirements.getAllergensToAvoid()
                            .stream()
                            .map(AllergenDTO::getName)
                            .toList()
            );
        }

        if (requirements.getSuitableForTheseDiets() != null && !requirements.getSuitableForTheseDiets().isEmpty()) {
            query += "AND COALESCE(dts.name, '') IN (:diets) ";
            params.put("diets", requirements.getSuitableForTheseDiets()
                    .stream()
                    .map(DietDTO::getName)
                    .toList()
            );
        }

        if (requirements.getNutrients() != null && !requirements.getNutrients().isEmpty()) {
            query += "AND n.id IN (:nutrientIds) ";
            params.put("nutrientIds",
                    requirements.getNutrients()
                            .stream()
                            .map(NutrientDTO::getId)
                            .toList()
            );
        }

        if (requirements.getDifficulty() != null && !requirements.getDifficulty().isEmpty()) {
            query += "AND r.difficulty.name = :difficulty ";
            params.put("difficulty", requirements.getDifficulty());
        }

        return find(query, params).list();
    }

    @Transactional
    public void persistRecipeEntity(RecipeEntity recipeEntity) {
        try {
            persistAndFlush(recipeEntity);
        } catch (jakarta.validation.ConstraintViolationException e) {
            throw new jakarta.validation.ConstraintViolationException(
                    "Could not persist user entity in database due to validation error: " + e.getMessage(),
                    e.getConstraintViolations()
            );

        } catch (org.hibernate.exception.ConstraintViolationException e) {
            throw new org.hibernate.exception.ConstraintViolationException(
                    "Could not persist user entity in database due to database constraint violation: " + e.getMessage(),
                    e.getSQLException(),
                    e.getConstraintName()
            );
        }
    }
}
