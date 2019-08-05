package com.tangcheng.rest;

import com.alibaba.fastjson.JSONObject;
import com.tangcheng.app.business.config.RestTemplateConfig;
import com.tangcheng.app.domain.query.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by tang.cheng on 2016/10/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RestTemplateConfig.class)
public class RestTemplateApiTest {

    //待测试的服务需要先启动，
    // 否则会提示 Connect to localhost:9999 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect
    String domain = "http://localhost:80/test";
    /**
     * digest:
     * (1)get请求中只有url，使用get打着的api，queryString必须拼到urk中。
     * 如果使用可变参数传入，变量名和urk中的变量名 不要求必须一致，只要与url中出现的顺序一致就可以了
     * 如果需要传入header信息，则需要使用exchange相关的api
     * (2)post请求中将url分拆成uri和body。
     * 需要传入的header信息可以放到HttpEntity类型的request参数中
     * 需要传入的body信息，可以通过add方法放到org.springframework.util.MultiValueMap类型的集合中
     * post打头api的第二个参数request可以使用HttpEntity来封装header、body信息
     * request post 请求中的body有四种类型：
     * application/x-www-form-urlencoded
     * multipart/form-data
     * application/json
     * text/xml
     */

    @Autowired
    private RestTemplate restTemplate;

    @Test
    //ResponseEntity<T> getForEntity(URI url, Class<T> responseType)
    public void testGetForEntity_URI() {
        String userId = "1";
        String url = domain + "/" + userId;

        ResponseEntity<Result> forEntity = restTemplate.getForEntity(URI.create(url), Result.class);
        Result body = forEntity.getBody();
        assertThat(body.getUserId(), is(userId));
    }

    @Test
    //ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... urlVariables)
    public void testGetForEntity_Varargs() {

        String url = domain + "/{userId}";
        String userId = "1";

        ResponseEntity<Result> forEntity = restTemplate.getForEntity(url, Result.class, userId);
        Result body = forEntity.getBody();
        assertThat(body.getUserId(), is(userId));
    }

    @Test
    //ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> urlVariables)
    public void testGetForEntity_Map() {
        //如果请求的URL是这种形式/id{userId},则用户1实际请求的url是这样的/id1
        String url = domain + "/{userId}";
        String userId = "1";
        Map<String, String> urlVariables = new HashMap<>();
        /**
         urlVariables（map）中的变量名必须与PathVariable中的点位符（eg:{userId}）保持一致。
         否则报下面的错误：
         java.lang.IllegalArgumentException: Map has no value for 'userId'
         at org.springframework.web.util.UriComponents$MapTemplateVariables.getValue(UriComponents.java:282)
         */
        urlVariables.put("userId", userId);
        ResponseEntity<Result> forEntity = restTemplate.getForEntity(url, Result.class, urlVariables);
        Result body = forEntity.getBody();
        assertThat(body.getUserId(), is(userId));
    }

    @Test
    public void testGetForEntityWithRequestParam_Varargs() {
        //因为get请求没有请求体，RequestParam只能拼到url字符串中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;
        getForEntityWithRequestParam(userId, pageId, pageSize);
    }

    @Test
    public void testGetForEntityWithRequestParam_Varargs_ignoreParamName() {
        //因为get请求没有请求体，RequestParam只能拼到url字符串中

        //使用可变参数往url中传入数据，不要求 变量名 与 url中的变量名 必须一致，只要顺序依次写入就可以了
        String firstParameter = "1";
        int second = 0;
        int third = 10;
        getForEntityWithRequestParam(firstParameter, second, third);
    }

    private void getForEntityWithRequestParam(String userId, int pageId, int pageSize) {
        Result excepted = new Result(userId, pageId, pageSize);
        String url = domain + "/{userId}/detail?pageId={pageId}&pageSize={pageSize}";
        ResponseEntity<Result> forEntity = restTemplate.getForEntity(url, Result.class, userId, pageId, pageSize);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }

    @Test
    public void testGetForEntityWithRequestParam_Map() {
        //因为get请求没有请求体，RequestParam只能拼到url字符串中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;

        Result excepted = new Result(userId, pageId, pageSize);

        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("userId", userId);
        urlVariables.put("pageId", pageId);
        urlVariables.put("pageSize", pageSize);

        String url = domain + "/{userId}/detail?pageId={pageId}&pageSize={pageSize}";
        /**
         *
         * 如果不将queryString拼到url中这些参数在服务器端是收到 不到的
         * eg:
         MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<>();
         urlVariables.add("userId", userId);
         urlVariables.add("pageId", pageId);
         urlVariables.add("pageSize", pageSize);

         String url = domain + "/user/{userId}/detail";
         ResponseEntity<Result> forEntity = restTemplate.getForEntity(url, Result.class, urlVariables);

         Expected :is <Result{userId='1', pageId=0, pageSize=10, auth='null'}>
         Actual   :<Result{userId='[1]', pageId=null, pageSize=null, auth='null'}>
         */
        ResponseEntity<Result> forEntity = restTemplate.getForEntity(url, Result.class, urlVariables);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }


    @Test
    public void testExchangeForGetWithRequestParam_Map_Header() {
        //因为get请求没有请求体，RequestParam只能拼到url字符串中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;
        String auth = "secret";

        Result excepted = new Result(userId, pageId, pageSize, auth);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("userId", userId);
        uriVariables.put("pageId", pageId);
        uriVariables.put("pageSize", pageSize);

        String url = domain + "/{userId}/auth?pageId={pageId}&pageSize={pageSize}";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("auth", auth);
        HttpEntity<?> requestEntity = new HttpEntity<>(header);

        ResponseEntity<Result> forEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Result.class, uriVariables);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }


    @Test
    //ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables)
    public void testPostForEntity_Varargs() {
        String url = domain + "/{userId}";
        String userId = "1";
        Result expected = new Result(userId);

        Object request = null;//因为除了Uri没有其它参数，request就是null
        ResponseEntity<Result> forEntity = restTemplate.postForEntity(url, request, Result.class, userId);
        Result body = forEntity.getBody();
        assertThat(body, is(expected));
    }


    @Test
    //ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables)
    public void testPostForEntity_Map() {
        String url = domain + "/{userId}";
        String userId = "1";
        Result expected = new Result(userId);

        Map<String, String> uriVariables = new HashMap<>();
        /**
         uriVariables（map）中的变量名必须与PathVariable中的点位符（eg:{userId}）保持一致。
         否则报下面的错误：
         java.lang.IllegalArgumentException: Map has no value for 'userId'
         at org.springframework.web.util.UriComponents$MapTemplateVariables.getValue(UriComponents.java:282)
         */
        uriVariables.put("userId", userId);

        Object request = null;//因为除了Uri没有其它参数，request就是null
        ResponseEntity<Result> forEntity = restTemplate.postForEntity(url, request, Result.class, uriVariables);
        Result body = forEntity.getBody();
        assertThat(body, is(expected));
    }


    @Test
    public void testPostForEntityWithRequestParam_Map() {
        //因为post请求有请求体application/x-www-form-urlencoded，RequestParam需要拼到url字符串中，
        // 因为postForEntity中没有urlVariables,只有uriVariables,因此RequestParam只能放到request中的form请求体中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;

        Result excepted = new Result(userId, pageId, pageSize);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("userId", userId);

        String url = domain + "/{userId}/detail";//userId是uriVariables；?pageId={pageId}&pageSize={pageSize}不是uri,需要放在request请求体中
        //如果给定一个MultiValueMap<String,String>，
        // 那么这个Map中的值将会被FormHttpMessageConverter以"application/x-www-form-urlencoded"的格式写到请求体中
        //java.util.Map中存放的数据，不会放到request请求的请求体中，只能是org.springframework.util.MultiValueMap
        MultiValueMap<String, Integer> request = new LinkedMultiValueMap<>();
        request.add("pageId", pageId);
        request.add("pageSize", pageSize);
        ResponseEntity<Result> forEntity = restTemplate.postForEntity(url, request, Result.class, uriVariables);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }


    @Test
    public void testPostForEntityWithRequestParam_Map_Header() {
        //因为post请求有请求体application/x-www-form-urlencoded，RequestParam需要拼到url字符串中，
        // 因为postForEntity中没有urlVariables,只有uriVariables,因此RequestParam只能放到request中的form请求体中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;
        String auth = "secret In Header";
        Result excepted = new Result(userId, pageId, pageSize, auth);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("userId", userId);

        String url = domain + "/{userId}/auth";//userId是uriVariables；?pageId={pageId}&pageSize={pageSize}不是uri,需要放在request请求体中

        //Header只能使用MultiValueMap类型
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("auth", auth);

        //如果给定一个MultiValueMap<String,String>，
        // 那么这个Map中的值将会被FormHttpMessageConverter以"application/x-www-form-urlencoded"的格式写到请求体中
        //java.util.Map中存放的数据，不会放到request请求的请求体中，只能是org.springframework.util.MultiValueMap
        MultiValueMap<String, Integer> body = new LinkedMultiValueMap<>();
        body.add("pageId", pageId);
        body.add("pageSize", pageSize);

        HttpEntity<MultiValueMap<String, Integer>> request = new HttpEntity<>(body, header);
        ResponseEntity<Result> forEntity = restTemplate.postForEntity(url, request, Result.class, uriVariables);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }


    @Test
    public void testPostJsonData() {
//        https://gxnotes.com/article/61287.html
        // create request body
        JSONObject request = new JSONObject();
        request.put("username", "name");
        request.put("password", "pwd");

        // set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);

        // send request and parse result
        ResponseEntity<String> loginResponse = restTemplate.exchange("", HttpMethod.POST, entity, String.class);
        if (loginResponse.getStatusCode() == HttpStatus.OK) {
        } else if (loginResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            // nono... bad credentials
        }
    }


    @Test
    public void testExchangeForPostWithRequestParam_Map_Header() {
        //因为post请求有请求体application/x-www-form-urlencoded，RequestParam需要拼到url字符串中，
        // 因为postForEntity中没有urlVariables,只有uriVariables,因此RequestParam只能放到request中的form请求体中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;
        String auth = "secret In Header";
        Result excepted = new Result(userId, pageId, pageSize, auth);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("userId", userId);

        String url = domain + "/{userId}/auth";//userId是uriVariables；?pageId={pageId}&pageSize={pageSize}不是uri,需要放在request请求体中

        //Header只能使用MultiValueMap类型
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("auth", auth);

        //如果给定一个MultiValueMap<String,String>，
        // 那么这个Map中的值将会被FormHttpMessageConverter以"application/x-www-form-urlencoded"的格式写到请求体中
        //java.util.Map中存放的数据，不会放到request请求的请求体中，只能是org.springframework.util.MultiValueMap
        MultiValueMap<String, Integer> body = new LinkedMultiValueMap<>();
        body.add("pageId", pageId);
        body.add("pageSize", pageSize);

        HttpEntity<MultiValueMap<String, Integer>> request = new HttpEntity<>(body, header);
        ResponseEntity<Result> forEntity = restTemplate.exchange(url, HttpMethod.POST, request, Result.class, uriVariables);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }


    @Test
    public void testExchangeWithRequestEntityForPostWithRequestParam_Map_Header() {
        //因为post请求有请求体application/x-www-form-urlencoded，RequestParam需要拼到url字符串中，
        // 因为postForEntity中没有urlVariables,只有uriVariables,因此RequestParam只能放到request中的form请求体中
        String userId = "1";
        int pageId = 0;
        int pageSize = 10;
        String auth = "secret In Header";
        Result excepted = new Result(userId, pageId, pageSize, auth);

        String url = domain + "/" + userId + "/auth";//userId是uriVariables；?pageId={pageId}&pageSize={pageSize}不是uri,需要放在request请求体中

        //Header只能使用MultiValueMap类型
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("auth", auth);

        //如果给定一个MultiValueMap<String,String>，
        // 那么这个Map中的值将会被FormHttpMessageConverter以"application/x-www-form-urlencoded"的格式写到请求体中
        //java.util.Map中存放的数据，不会放到request请求的请求体中，只能是org.springframework.util.MultiValueMap
        MultiValueMap<String, Integer> body = new LinkedMultiValueMap<>();
        body.add("pageId", pageId);
        body.add("pageSize", pageSize);

        //RequestEntity中只有有body,header，PathVariable这种参数，就只能自己拼字符串喽
        RequestEntity<MultiValueMap<String, Integer>> requestEntity = new RequestEntity<>(body, header, HttpMethod.POST, URI.create(url));
        ResponseEntity<Result> forEntity = restTemplate.exchange(requestEntity, Result.class);
        Result actual = forEntity.getBody();
        assertThat(actual, is(excepted));
    }


    @Test
    public void givenApiWithGenericReturnType_thenUseExchange() {
        /**
         * 返回值为泛型的场景：借助于ParameterizedTypeReference和HttpHeader指定contentType
         */
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(new URI("http://localhost:8080/test/session/get"), HttpMethod.GET, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
            });
            assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
            System.out.println(responseEntity.getBody());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


}
