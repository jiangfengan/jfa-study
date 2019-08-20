import com.jfa.startup.Demo;
import org.junit.Test;

public class HelloWorld {

    @Test
    public void testSayHello(){
        Demo demo=new Demo();
        System.out.println(demo.sayHello());
    }
}
