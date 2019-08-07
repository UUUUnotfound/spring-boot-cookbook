package com.tangcheng.app.api.rest.config;

import com.tangcheng.app.api.rest.filter.AddExtraToParamsFilter;
import com.tangcheng.app.api.rest.filter.RewriteServletPathFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;

/**
 * Created by MyWorld on 2016/9/25.
 */
@Configuration
public class FilterConfig {

    /**
     * 定义多个FilterRegistrationBean，都会生效，类似于web.xml中的定义
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean addExtraToParamsFilter() {
        /**
         * 在1.4.0的时候，包名从
         * org.springframework.boot.context.embedded.FilterRegistrationBean
         * 变为了
         * import org.springframework.boot.web.servlet.FilterRegistrationBean需要注意一下。
         */
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new AddExtraToParamsFilter());
        filterRegistrationBean.setOrder(2);
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RewriteServletPathFilter());
        filterRegistrationBean.setOrder(3);
        return filterRegistrationBean;
    }

    @Bean
    public Filter etagFilter() {
        return new ShallowEtagHeaderFilter();
    }

}
