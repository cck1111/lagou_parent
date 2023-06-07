package com.lagou.goods.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author cck
 * @date 2023/6/6 10:09
 */
@Table(name="tb_category_brand")
public class CategoryBrand {
    @Id
    private Integer categoryId;
    @Id
    private Integer brandId;
    public Integer getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    public Integer getBrandId() {
        return brandId;
    }
    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }
}
