package com.zcw.simpledata;

import com.zcw.simpledata.base.annotations.EnableSimpleData;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/***
 * simple-data
 * @author zcw
 * @version 0.0.1
 */

@SpringBootApplication
@EnableSimpleData(initClass = true)
public class SimpleDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleDataApplication.class, args);
    }

}
