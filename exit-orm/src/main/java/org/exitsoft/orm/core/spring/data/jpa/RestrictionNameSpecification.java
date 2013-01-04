package org.exitsoft.orm.core.spring.data.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

/**
 * 实现spring data jpa的{@link Specification}接口，通过该类支持对象属性名查询方法
 * 
 * @author vincent
 *
 * @param <T> orm 对象
 */
public class RestrictionNameSpecification<T> implements Specification<T>{
	
	private JpaRestrictionBuilder restrictionBuilder = new JpaRestrictionBuilder();
	//属性名称
	private String propertyName;
	//值
	private Object value;
	//约束名称
	private String restrictionName;
	
	public RestrictionNameSpecification() {
		
	}
	/**
	 * 构造属性名查询Specification
	 * 
	 * @param propertyName 对象属性名
	 * @param value 值
	 * @param restrictionName 约束名称
	 */
	public RestrictionNameSpecification(String propertyName, Object value,String restrictionName) {
		this.propertyName = propertyName;
		this.value = value;
		this.restrictionName = restrictionName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.jpa.domain.Specification#toPredicate(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.CriteriaBuilder)
	 */
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,CriteriaBuilder builder) {
		
		restrictionBuilder.setSpecificationProperty(root,query,builder);
		
		Predicate predicate = builder.and(restrictionBuilder.getRestriction(propertyName, value, restrictionName));
		
		restrictionBuilder.clearSpecificationProperty();
		
		return predicate;
	}

}