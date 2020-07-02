package ru.list.surkovr.skblab.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorsResponseDto extends BaseResponseDto {

    private List<String> errors;

    public ErrorsResponseDto(boolean success, List<String> errors) {
        super(success);
        this.errors = errors;
    }

    public ErrorsResponseDto(boolean success) {
        super(success);
    }

}
