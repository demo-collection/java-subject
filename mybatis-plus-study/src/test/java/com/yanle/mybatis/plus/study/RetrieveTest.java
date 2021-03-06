package com.yanle.mybatis.plus.study;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanle.mybatis.plus.study.dao.UserMapper;
import com.yanle.mybatis.plus.study.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RetrieveTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void selectById() {
        User user = userMapper.selectById(1094592041087729666L);
        System.out.println(user);
    }

    @Test
    public void batchSelectById() {
        List<Long> ids = Arrays.asList(1087982257332887553L, 1088248166370832385L, 1088250446457389058L);
        List<User> userList = userMapper.selectBatchIds(ids);
        Assert.assertEquals(3, userList.size());
        userList.forEach(item -> System.out.println(item));
    }

    @Test
    public void selectByMap() {
        // string 必须要对应表field名称
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("manager_id", 1088248166370832385L);
        columnMap.put("name", "张雨琪");
        // 效果等价于 where name = '张雨琪' and manager_id = 1088248166370832385L
        List<User> userList = userMapper.selectByMap(columnMap);
        userList.forEach(System.out::println);
    }

    @Test
    public void selectByMapMultiple() {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("manager_id", 1088248166370832385L);
        List<User> userList = userMapper.selectByMap(columnMap);
        userList.forEach(System.out::println);
    }

    /*
    名字中包含雨并且年龄小于40
	name like '%雨%' and age<40
    * */
    @Test
    public void selectByWrapper() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();
        queryWrapper.like("name", "雨").lt("age", 40);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    名字中包含雨年并且龄大于等于20且小于等于40并且email不为空
    name like '%雨%' and age between 20 and 40 and email is not null
    * */
    @Test
    public void selectByWrapper2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();
        queryWrapper.like("name", "雨").between("age", 20, 40).isNotNull("email");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    名字为王姓或者年龄大于等于25，按照年龄降序排列，年龄相同按照id升序排列
    name like '王%' or age>=25 order by age desc,id asc
    * */
    @Test
    public void selectByWrapper3() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();
        queryWrapper.likeRight("name", "王").or().ge("age", 25).orderByDesc("age")
                .orderByAsc("id");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    创建日期为2019年2月14日并且直属上级为名字为王姓
    date_format(create_time,'%Y-%m-%d')='2019-02-14' and manager_id
    in (select id from user where name like '王%')
    * */
    @Test
    public void selectByWrapper4() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();
        queryWrapper.apply("date_format(create_time,'%Y-%m-%d')={0}", "2019-02-14")
                .inSql("manager_id", "select id from user where name like '王%'");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    名字为王姓并且（年龄小于40或邮箱不为空）
    name like '王%' and (age<40 or email is not null)
    * */
    @Test
    public void selectByWrapper5() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name", "王").and(wq -> wq.lt("age", 40).or().isNotNull("email"));
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /*
    名字为王姓或者（年龄小于40并且年龄大于20并且邮箱不为空）
    name like '王%' or (age<40 and age>20 and email is not null)
    * */
    @Test
    public void selectByWrapper6() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("name", "王").or(
                wq -> wq.lt("age", 40).gt("age", 20).isNotNull("email")
        );
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    (年龄小于40或邮箱不为空）并且名字为王姓
    (age<40 or email is not null) and name like '王%'
    * */
    @Test
    public void selectByWrapper7() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.nested(qw -> qw.lt("age", 40).or().isNotNull("email"))
                .likeRight("name", "王");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    年龄为30、31、34、35
    age in (30、31、34、35)
    * */
    @Test
    public void selectByWrapper8() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("age", Arrays.asList(30, 31, 34, 35));
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    只返回满足条件的其中一条语句即可
    limit 1
    * */
    @Test
    public void selectByWrapper9() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("age", Arrays.asList(30, 31, 34, 35)).last("limit 1");
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    select id,name
       from user
       where name like '%雨%' and age<40
    * */
    @Test
    public void selectByWrapperSupper() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name").like("name", "雨").lt("age", 40);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    select id,name,age,email
	           from user
	           where name like '%雨%' and age<40
    * */
    @Test
    public void selectByWrapperSupper2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "雨").lt("age", 40)
                .select(
                        User.class,
                        info -> !info.getColumn().equals("create_time") && !info.getColumn().equals("manager_id")
                );
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    带条件的查询
    * */
    @Test
    public void testCondition() {
        String name = "王";
        String email = "";
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), "name", name)
                .like(StringUtils.isNotEmpty(email), "email", email);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    实体类作为条件构造器构造方法参数
    * */
    @Test
    public void selectByWrapperEntity() {
        User whereUser = new User();
        whereUser.setName("刘红雨");
        whereUser.setAge(32);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(whereUser);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    实体类作为条件构造器构造方法参数
    作为实体类的场景
    还可以在实体类上加注解， 例如

    @TableField(condition = SqlCondition.LIKE)
    private String name;

    @TableField(condition = "%s&lt;#{%s}")
    private Integer age;
    * */
    @Test
    public void selectByWrapperEntity2() {
        User whereUser = new User();
        whereUser.setName("刘红雨");
        whereUser.setAge(32);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(whereUser);
        queryWrapper.like("name", "雨").lt("age", 40);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }


    /*
    allEq的用法
    * */
    @Test
    public void selectByWrapperAllEq() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Map<String, Object> params = new HashMap<>();
        params.put("name", "王天风");
        params.put("age", 25);
        queryWrapper.allEq(params);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    allEq的用法
    * */
    @Test
    public void selectByWrapperAllEq2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Map<String, Object> params = new HashMap<>();
        params.put("name", "王天风");
        params.put("age", null);
        // 第二个值， 如果是false, 如果是一个null， 就过滤掉
        queryWrapper.allEq(params, false);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    条件语句的 allEq 使用方法
    * */
    @Test
    public void selectByWrapperAllEq3() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Map<String, Object> params = new HashMap<>();
        params.put("name", "王天风");
        params.put("age", null);
        // 第二个值， 如果是false, 如果是一个null， 就过滤掉
        queryWrapper.allEq((key, value) -> !key.equals("name"), params);
        List<User> userList = userMapper.selectList(queryWrapper);
        userList.forEach(System.out::println);
    }

    @Test
    public void selectByWrapperMaps() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();
        queryWrapper.select("id", "name").like("name", "雨").lt("age", 40);
        List<Map<String, Object>> userList = userMapper.selectMaps(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    11、按照直属上级分组，查询每组的平均年龄、最大年龄、最小年龄。
        并且只取年龄总和小于500的组。
        select avg(age) avg_age,min(age) min_age,max(age) max_age
        from user
        group by manager_id
        having sum(age) <500
    * */
    @Test
    public void selectByWrapperMaps2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();

        queryWrapper.select("avg(age) avg_age", "min(age) min_age", "max(age) max_age")
                .groupBy("manager_id").having("sum(age) < {0}", 500);

        List<Map<String, Object>> userList = userMapper.selectMaps(queryWrapper);
        userList.forEach(System.out::println);
    }

    @Test
    public void selectByWrapperObjs() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 上面也可以类似于写成这样
        // QueryWrapper<User> query = Wrappers.<User>query();

        queryWrapper.like("name", "雨").lt("age", 40);

        List<Object> userList = userMapper.selectObjs(queryWrapper);
        userList.forEach(System.out::println);
    }

    /*
    其他: selectCount、selectOne、
    * */


    /*
    lambda 条件构造器
    * */
    @Test
    public void selectLambda() {
//        // 方式1
//        LambdaQueryWrapper<User> lambda = new QueryWrapper<User>().lambda();
//
//        // 方式2
//        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<User>();

        // 方式3
        LambdaQueryWrapper<User> lambdaQuery = Wrappers.<User>lambdaQuery();

        // where name like '%雨%' and age < 40;
        lambdaQuery.like(User::getName, "雨").lt(User::getAge, 40);
        List<User> userList = userMapper.selectList(lambdaQuery);
        userList.forEach(System.out::println);
    }

    @Test
    public void selectLambda2() {
//        // 方式1
//        LambdaQueryWrapper<User> lambda = new QueryWrapper<User>().lambda();

        // 方式2
        LambdaQueryWrapper<User> lambdaQuery = new LambdaQueryWrapper<User>();

//        // 方式3
//        LambdaQueryWrapper<User> lambdaQuery = Wrappers.<User>lambdaQuery();

        lambdaQuery.likeRight(User::getName, "王").and(
                lqw -> lqw.lt(User::getAge, 40).or().isNotNull(User::getEmail)
        );
        List<User> userList = userMapper.selectList(lambdaQuery);
        userList.forEach(System.out::println);
    }

    @Test
    public void selectLambda3() {
        List<User> userList = new LambdaQueryChainWrapper<User>(userMapper)
                .like(User::getName, "雨")
                .ge(User::getAge, 20)
                .list();
        userList.forEach(System.out::println);
    }

    /*
    直接通过自定义注解sql的方式来添加sql:
    @Select("select * from user ${ew.customSqlSegment}")
    List<User> selectAll(@Param(Constants.WRAPPER) Wrapper<User> wrapper);
    * */
    @Test
    public void selectMy() {
        // 方式2
        LambdaQueryWrapper<User> lambdaQuery = new LambdaQueryWrapper<User>();

        lambdaQuery.likeRight(User::getName, "王").and(
                lqw -> lqw.lt(User::getAge, 40).or().isNotNull(User::getEmail)
        );
        List<User> userList = userMapper.selectAll(lambdaQuery);
        userList.forEach(System.out::println);
    }

    /*
    定义xml的方式

    首先要定义扫描xml的路径
    application.yml
    mybatis-plus:
        mapper-locations: classpath:mapper/*.xml

    按照正常 mybatis 操作就可以了
    src/main/resources/mapper/UserMapper.xml
    * */


    /*
    分页
    * */
    @Test
    public void selectPage() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("age", 26);
        Page<User> page = new Page<User>(1, 2);
        IPage<User> iPage = userMapper.selectPage(page, queryWrapper);
        System.out.println("总页数" + iPage.getPages());
        System.out.println("总记录数" + iPage.getTotal());
        List<User> userList = iPage.getRecords();
        userList.forEach(System.out::println);
    }

    /*
    分页
    userMapper.selectMapsPage(page, queryWrapper);
    * */
    @Test
    public void selectPage2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("age", 26);
        Page<Map<String, Object>> page = new Page<>(1, 2);
        IPage<Map<String, Object>> iPage = userMapper.selectMapsPage(page, queryWrapper);
        System.out.println("总页数" + iPage.getPages());
        System.out.println("总记录数" + iPage.getTotal());
        List<Map<String, Object>> userList = iPage.getRecords();
        userList.forEach(System.out::println);
    }

    /*
    分页
     new Page<>(1, 2, false);
    * */
    @Test
    public void selectPage3() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("age", 26);
        // 第三个参数是 false 不查询总记录数量
        Page<Map<String, Object>> page = new Page<>(1, 2, false);
        IPage<Map<String, Object>> iPage = userMapper.selectMapsPage(page, queryWrapper);
        System.out.println("总页数" + iPage.getPages());
        System.out.println("总记录数" + iPage.getTotal());
        List<Map<String, Object>> userList = iPage.getRecords();
        userList.forEach(System.out::println);
    }

    /*
    分页
    自定义xml查询
    * */
    @Test
    public void selectMyPage() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("age", 26);
        // 第三个参数是 false 不查询总记录数量
        Page<User> page = new Page<>(1, 2);
        IPage<User> iPage = userMapper.selectUserPage(page, queryWrapper);
        System.out.println("总页数" + iPage.getPages());
        System.out.println("总记录数" + iPage.getTotal());
        List<User> userList = iPage.getRecords();
        userList.forEach(System.out::println);
    }
}
