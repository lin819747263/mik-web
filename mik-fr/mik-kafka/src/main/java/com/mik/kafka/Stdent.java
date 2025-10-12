package com.mik.kafka;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Stdent {
    private String name;
    private int age;
    private String time;
}
