package com.example.duantn.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;

@Entity
@Table(name = "DotKhuyenMai")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DotKhuyenMai extends BaseModel {
    @Column(name = "Ma")
    private String ma;

    @Column(name = "tenDotKhuyenMai")
    private String ten;

    @Column(name = "GiaTriGiam")
    private int giaTriGiam;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayBatDau")
    private Date ngayBatDau;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "NgayKetThuc")
    private Date ngayKetThuc;

}
