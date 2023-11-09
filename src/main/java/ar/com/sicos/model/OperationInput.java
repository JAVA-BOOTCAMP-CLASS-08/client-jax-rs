package ar.com.sicos.model;

import lombok.Data;

@Data
public class OperationInput {

    private Integer value1;
    private Integer value2;

    private Operation operation;
}
