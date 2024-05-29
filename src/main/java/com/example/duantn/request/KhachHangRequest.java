package com.example.duantn.request;

import com.example.duantn.model.BaseModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KhachHangRequest extends BaseModel {
//    @NotBlank(message = "{giohang.taikhoan.notblank}")
    private String taiKhoan;

    //    @NotBlank(message = "{giohang.matkhau.notblank}")
    private String matKhau;

    private UUID id;
    @NotBlank(message = "Vui lòng nhập mã khách hàng")
    private String ma;

    @NotBlank(message = "Vui lòng nhập họ và tên khách hàng")
    @Size(max=50, message = "Tên khách hàng chỉ được nhập tối đa 50 ký tự")
    private String hoTen ;

    //
//    @NotBlank(message = "{khachHang.matKhau.notblank}")
//    private String matKhau;
//
    @NotNull(message = "Vui lòng chọn giới tính")
    private Integer gioiTinh;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(min = 10,max = 10, message = "số điện thoại không hợp lệ")
    private String soDT;

    @NotBlank(message = "Vui lòng nhập email")
    private String email;

//    @NotNull(message = "{khachHang.trangThai.notnull}")
//    private Integer trangThai;
}