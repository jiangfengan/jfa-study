package com.jfa.demo;

import org.springframework.stereotype.Component;

@Component
public class PoSanServiceImport implements PoScan {

    public String sayHello() {
        return "Hello ";
    }
}
