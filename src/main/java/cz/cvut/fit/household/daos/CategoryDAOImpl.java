package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.repository.CategoryRepository;
import cz.cvut.fit.household.daos.interfaces.CategoryDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryDAOImpl implements CategoryDAO {

    private final CategoryRepository categoryRepository;
    @Override
    public Category saveCategory (Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.getCategoryById(id);
    }
}
