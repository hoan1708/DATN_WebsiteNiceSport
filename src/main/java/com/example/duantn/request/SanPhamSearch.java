package com.example.duantn.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public interface SanPhamSearch {
    UUID getIdSanPham();
    String getTenSanPham();
    Integer getTongSoLuongSP();
    String getTrangThaiSP();
}

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class SanPhamSearch {
//    private Long idSanPham;
//    private String tenSanPham;
//    private Long tongSoLuongSP;
//    private String trangThaiSP;
//}
