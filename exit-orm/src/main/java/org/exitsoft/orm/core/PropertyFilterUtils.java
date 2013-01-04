package org.exitsoft.orm.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.exitsoft.common.utils.ServletUtils;
import org.springframework.util.Assert;

/**
 * 属性过滤器工具类
 * 
 * @author vincent
 *
 */
public abstract class PropertyFilterUtils {
	
	/**
	 * 通过表达式和对比值创建属性过滤器集合,要求表达式与值必须相等
	 * <p>
	 * 	如：
	 * </p>
	 * <code>
	 * 	PropertyFilerRestriction.createrPropertyFilter(new String[]{"EQS_propertyName1","NEI_propertyName2"},new String[]{"vincent","vincent_OR_admin"})
	 * </code>
	 * <p>
	 * 	对比值长度与表达式长度必须相等
	 * </p>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return List
	 */
	public static List<PropertyFilter> createPropertyFilters(String[] expressions,String[] matchValues) {
		if (ArrayUtils.isEmpty(expressions) && ArrayUtils.isEmpty(matchValues)) {
			return Collections.emptyList();
		}
		
		if (expressions.length != matchValues.length) {
			throw new IllegalAccessError("expressions中的值与matchValues不匹配，matchValues的长度为:" + matchValues.length + "而expressions的长度为:" + expressions.length);
		}
		
		List<PropertyFilter> filters = new ArrayList<PropertyFilter>();
		
		for (int i = 0; i < expressions.length; i++) {
			filters.add(createPropertyFilter(expressions[i], matchValues[i]));
		}
		
		return filters;
	}
	
	/**
	 * 通过表达式和对比值创建属性过滤器
	 * <p>
	 * 	如：
	 * </p>
	 * <code>
	 * 	PropertyFilerRestriction.createrPropertyFilter("EQS_propertyName","vincent")
	 * </code>
	 * 
	 * @param expressions 表达式
	 * @param matchValues 对比值
	 * 
	 * @return {@link PropertyFilter}
	 */
	public static PropertyFilter createPropertyFilter(String expression,String matchValue) {
		
		Assert.hasText(expression, "表达式不能为空");
		
		String restrictionsNameAndClassType = StringUtils.substringBefore(expression, "_");
		
		String restrictionsName = StringUtils.substring(restrictionsNameAndClassType, 0,restrictionsNameAndClassType.length() - 1);
		String classType = StringUtils.substring(restrictionsNameAndClassType, restrictionsNameAndClassType.length() - 1, restrictionsNameAndClassType.length());
		
		PropertyType propertyType = null;
		try {
			propertyType = PropertyType.valueOf(classType);
		} catch (Exception e) {
			throw new IllegalAccessError("[" + expression + "]表达式找不到相应的属性类型,获取的值为:" + classType);
		}
		
		String[] propertyNames = null;
		
		if (StringUtils.contains(expression,"_OR_")) {
			String temp = StringUtils.substringAfter(expression, restrictionsNameAndClassType + "_");
			propertyNames = StringUtils.splitByWholeSeparator(temp, "_OR_");
		} else {
			propertyNames = new String[1];
			propertyNames[0] = StringUtils.substringAfterLast(expression, "_");
		}
		
		return new PropertyFilter(restrictionsName, propertyType, propertyNames,matchValue);
	}
	
	/**
	 * 从HttpRequest参数中创建PropertyFilter列表, 默认Filter属性名前缀为filter.
	 * 当参数存在{filter_EQS_property1:value,filter_EQS_property2:''}该形式的时候，将不会创建filter_EQS_property2等于""值的实例
	 * 参考{@link PropertyFilterBuilder#buildPropertyFilter(HttpServletRequest, String, boolean)}
	 * 
	 * @param request HttpServletRequest
	 */
	public static List<PropertyFilter> buildFromHttpRequest(HttpServletRequest request) {
		return buildFromHttpRequest(request, "filter");
	}
	
	/**
	 * 从HttpRequest参数中创建PropertyFilter列表,当参数存在{filter_EQS_property1:value,filter_EQS_property2:''}
	 * 该形式的时候，将不会创建filter_EQS_property2等于""值的实例
	 * 参考{@link PropertyFilterBuilder#buildPropertyFilter(HttpServletRequest, String, boolean)}
	 * 
	 * @param request HttpServletRequest
	 * @param filterPrefix 用于识别是propertyfilter参数的前准
	 * 
	 * @return List
	 */
	public static List<PropertyFilter> buildFromHttpRequest(HttpServletRequest request,String filterPrefix) {
		return buildPropertyFilter(request, "filter",false);
	}
	
	/**
	 * 从HttpRequest参数中创建PropertyFilter列表,当参数存在{filter_EQS_property1:value,filter_EQS_property2:''}
	 * 该形式的时候，将不会创建filter_EQS_property2等于""值的实例
	 * 参考{@link PropertyFilterBuilder#buildPropertyFilter(HttpServletRequest, String, boolean)}
	 * 
	 * <pre>
	 * 当页面提交的参数为:{filter_EQS_property1:value,filter_EQS_property2:''}
	 * List filters =buildPropertyFilter(request,"filter",false);
	 * 当前filters:EQS_proerpty1="value",EQS_proerpty1=""
	 * 
	 * 当页面提交的参数为:{filter_EQS_property1:value,filter_EQS_property2:''}
	 * List filters =buildPropertyFilter(request,"filter",true);
	 * 当前filters:EQS_proerpty1="value"
	 * </pre>
	 * 
	 * @param request HttpServletRequest
	 * @param ignoreEmptyValue true表示当存在""值时忽略该PropertyFilter
	 * 
	 * @return List
	 */
	public static List<PropertyFilter> buildFromHttpRequest(HttpServletRequest request,boolean ignoreEmptyValue) {
		return buildPropertyFilter(request, "filter",ignoreEmptyValue);
	}

	/**
	 * 从HttpRequest参数中创建PropertyFilter列表,例子:
	 * 
	 * <pre>
	 * 当页面提交的参数为:{filter_EQS_property1:value,filter_EQS_property2:''}
	 * List filters =buildPropertyFilter(request,"filter",false);
	 * 当前filters:EQS_proerpty1="value",EQS_proerpty1=""
	 * 
	 * 当页面提交的参数为:{filter_EQS_property1:value,filter_EQS_property2:''}
	 * List filters =buildPropertyFilter(request,"filter",true);
	 * 当前filters:EQS_proerpty1="value"
	 * </pre>
	 * 
	 * @param request HttpServletRequest
	 * @param filterPrefix 用于识别是propertyfilter参数的前准
	 * @param ignoreEmptyValue true表示当存在""值时忽略该PropertyFilter
	 * 
	 * @return List
	 */
	public static List<PropertyFilter> buildPropertyFilter(HttpServletRequest request,String filterPrefix,boolean ignoreEmptyValue) {

		// 从request中获取含属性前缀名的参数,构造去除前缀名后的参数Map.
		Map<String, Object> filterParamMap = ServletUtils.getParametersStartingWith(request, filterPrefix + "_");

		return buildPropertyFilter(filterParamMap,ignoreEmptyValue);
	}
	
	/**
	 * 从Map中创建PropertyFilter列表，如:
	 * 
	 * <pre>
     * Map o = new HashMap();
	 * o.put("EQS_property1","value");
	 * o.put("EQS_property2","");
	 * List filters = buildPropertyFilter(o);
	 * 当前filters:EQS_proerpty1="value",EQS_proerpty1=""
     * </pre>
	 * 
	 * 
	 * @param filters 过滤器信息
	 * 
	 */
	public static List<PropertyFilter> buildPropertyFilter(Map<String, Object> filters) {
		
		return buildPropertyFilter(filters,false);
	}
	
	/**
	 * 从Map中创建PropertyFilter列表，如:
	 * 
	 * <pre>
     * Map o = new HashMap();
	 * o.put("EQS_property1","value");
	 * o.put("EQS_property2","");
	 * List filters = buildPropertyFilter(o,false);
	 * 当前filters:EQS_proerpty1="value",EQS_proerpty1=""
	 * 
	 * Map o = new HashMap();
	 * o.put("EQS_property1","value");
	 * o.put("EQS_property2","");
	 * List filters = buildPropertyFilter(o,true);
	 * 当前filters:EQS_proerpty1="value"
     * </pre>
	 * 
	 * 
	 * @param filters 过滤器信息
	 * @param ignoreEmptyValue true表示当存在 null或者""值时忽略该PropertyFilter
	 * 
	 */
	public static List<PropertyFilter> buildPropertyFilter(Map<String, Object> filters,boolean ignoreEmptyValue) {
		List<PropertyFilter> filterList = new ArrayList<PropertyFilter>();
		// 分析参数Map,构造PropertyFilter列表
		for (Map.Entry<String, Object> entry : filters.entrySet()) {
			String expression = entry.getKey();
			Object value = entry.getValue();
			//如果ignoreEmptyValue为true忽略null或""的值
			if (ignoreEmptyValue && (value == null || value.toString().equals(""))) {
				continue;
			}
			//如果ignoreEmptyValue为true忽略null或""的值
			PropertyFilter filter = createPropertyFilter(expression, value.toString());
			filterList.add(filter);
			
		}
		return filterList;
	}
}