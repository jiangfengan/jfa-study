import com.jfa.demo.PoScan;
import com.jfa.startup.Demo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:spring.xml")
public class HelloWorld {

    @Autowired
    private PoScan poScan;

    @Test
    public void testSayHello(){
        Demo demo=new Demo();
        System.out.println(demo.sayHello());
    }

    @Test
    public void testSpringHello(){
        System.out.println(poScan.sayHello());
    }
}
