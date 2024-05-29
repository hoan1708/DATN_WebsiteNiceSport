package com.example.duantn.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class PhieuGiamGiaRequest {

    private UUID id;

    @NotBlank(message = "Vui lòng nhập mã phiếu")
    private String ma;

    @NotBlank(message = "Vui lòng nhập tên phiếu")
    @Length(min=5, max=100, message = "Vui lòng nhập tên tối thiếu 5 kí tự ")
    private String tenPhieu;

    @NotNull(message = "Vui lòng nhập giá trị")
    private Double giaTriGiam;

    @NotBlank(message = "Vui lòng nhập hình thức giảm")
    private String hinhThucGiam;

    @NotNull(message = "Vui lòng nhập giá trị")
    private Double giaTienXetDieuKien;

    @NotNull(message = "Vui lòng nhập ngày bắt đầu")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ngayBatDauApDung;

    @NotNull(message = "Vui lòng nhập ngày kết thúc")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date ngayKetThucApDung;

    @NotNull(message = "Vui lòng nhập số lượng")
    private Integer soLuong;

    private Integer trangThai;
}
