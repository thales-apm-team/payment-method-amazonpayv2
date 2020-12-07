package com.payline.payment.amazonv2.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Getter
@SuperBuilder
@AllArgsConstructor
public class AmazonBean {

    private Date creationTimestamp;
    private ReleaseEnvironment releaseEnvironment;


    public enum ReleaseEnvironment {
        Sandbox, Live
    }
}
