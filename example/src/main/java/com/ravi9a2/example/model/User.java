package com.ravi9a2.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User model class.
 * 
 * @author raviprakash
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String email;
    private int age;
}
