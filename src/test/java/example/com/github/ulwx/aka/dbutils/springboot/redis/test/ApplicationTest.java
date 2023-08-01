package example.com.github.ulwx.aka.dbutils.springboot.redis.test;


import com.github.ulwx.aka.dbutils.springboot.redis.AkaRedisSelector;
import com.ulwx.tool.Assert;
import com.ulwx.tool.RandomUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MyRedisService myRedisService;


    @Test
    public void test(){
        String  str= RandomUtils.genRandomString(10);
        myRedisService.setForOne("abc", str);
        String value=myRedisService.getForOne("abc");
        Assert.equal(value,str);

        str= RandomUtils.genRandomString(10);
        myRedisService.set("abc",str);
        value=myRedisService.get("abc");
        Assert.equal(value,str);

        AkaRedisSelector.push("default");
        value=myRedisService.get("abc");
        AkaRedisSelector.pop("default");
        Assert.equal(value,str);

    }




}
