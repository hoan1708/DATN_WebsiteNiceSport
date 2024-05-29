package com.example.duantn.controller;

import com.example.duantn.model.ChiTietSanPham;
import com.example.duantn.model.HoaDon;
import com.example.duantn.model.HoaDonChiTiet;
import com.example.duantn.repository.HoaDonCTRepository;
import com.example.duantn.repository.HoaDonRepository;
import com.example.duantn.repository.SanPhamCTRepository;
import com.example.duantn.request.GioHang;
import com.example.duantn.request.HoaDonCheckSoLuongTon;
import com.example.duantn.request.SanPhamTrongGioHang;
import com.example.duantn.service.HoaDonService;
import com.example.duantn.service.PhieuGiamGiaService;
import com.example.duantn.service.impl.ChiTietSPServiceImpl;
import com.example.duantn.service.impl.HoaDonCTServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.Date;
import java.util.*;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {
    @Autowired
    HoaDonRepository hoaDonRepository;
    @Autowired
    HoaDonService hoaDonService;
    @Autowired
    PhieuGiamGiaService phieuGiamGiaService;

    @Autowired
    HoaDonCTRepository hoaDonCTRepository;

    @Autowired
    SanPhamCTRepository sanPhamCTRepository;

    @Autowired
    HoaDonCTServiceImpl hoaDonCTService;

    @Autowired
    ChiTietSPServiceImpl chiTietSPService;

    @GetMapping("/hien-thi")
    public String getAllLoaiHoaDon(
            Model model,
            @RequestParam(defaultValue = "	0", name = "page") Integer pageNum,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "start_date", required = false) String startDate,
            @RequestParam(value = "end_date", required = false) String endDate,
            @RequestParam(value = "payment_method", required = false) String hinhThucThanhToan,
            @RequestParam(value = "status", required = false) String trangThai

//            @RequestParam(value = "payment_method", required = false) String hinhThucThanhToan,
//            @RequestParam(value = "status", required = false) List<Integer> trangThai
    ) {
//    	if( keyword.orElse(null) == null || keyword.orElse(null).isEmpty()) {
//    	    Page<HoaDon> pagehoaDon = hoaDonService.phanTrangHoaDon(pageNum, 5);
//            model.addAttribute("listHD", pagehoaDon);
//    	}else {
//
//
//    	      // Tạo đối tượng Pageable với sắp xếp và số trang, số lượng mỗi trang
//    	     Pageable pageable = PageRequest.of(pageNum, 5, sort);
//    		 Page<HoaDon> pagehoaDon = hoaDonRepository.findByMa(keyword.orElse(null), pageable);
//             model.addAttribute("listHD", pagehoaDon);
//    	}
//        Sort sort = Sort.by(Sort.Direction.DESC, "NgaySua");
//        Date start = startDate.orElse(null);
//        Date end = endDate.orElse(null);
        // Xử lý phân trang
        Pageable pageable = PageRequest.of(pageNum, 5);
        Page<HoaDon> pagehoaDon = null;
        System.out.println("Value == " + keyword + " ==  " + startDate + " == " + endDate + " == " + hinhThucThanhToan + " " + trangThai);
        if ((keyword == null || keyword.isEmpty()) && (startDate == null || startDate.isEmpty()) && (startDate == null || endDate.isEmpty()) && (hinhThucThanhToan == null || hinhThucThanhToan.isEmpty()) && (trangThai == null || trangThai.isEmpty())) {
            pagehoaDon = hoaDonRepository.searchAll(pageable);
        } else {
            System.out.println("startDSat" + startDate + "  " + endDate);
            Date start = (startDate == null || startDate.isEmpty()) ? null : Date.valueOf(startDate);
            Date end = (endDate == null || endDate.isEmpty()) ? null : Date.valueOf(endDate);
            pagehoaDon = hoaDonRepository.customSearch(keyword, start, end, hinhThucThanhToan, trangThai, pageable);
//            pagehoaDon = hoaDonRepository.customSearch(keyword, hinhThucThanhToan, trangThai, pageable);
        }
//        Page<HoaDon> pagehoaDon = hoaDonRepository.customSearch(keyword.orElse(""), start, end, hinhThucThanhToan, trangThai, pageable);
        System.out.println("page" + pagehoaDon + "  " + pagehoaDon.getContent() + "  " + pagehoaDon.getTotalElements());
        model.addAttribute("listHD", pagehoaDon);
        model.addAttribute("hoaDon", new HoaDon());
        model.addAttribute("listPG", phieuGiamGiaService.getAll());
        return "admin/HoaDonForm/trangChu";
    }

    // detail
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable String id, Model model) {
        HoaDon hoaDon = hoaDonService.detail(UUID.fromString(id));
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("listPG", phieuGiamGiaService.getAll());
        return "admin/HoaDonForm/update";
    }

    // delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        hoaDonService.delete(UUID.fromString(id));
        return "redirect:hoa-don/hien-thi";
    }

    // add
    @PostMapping("/add")
    public String add(@Validated @ModelAttribute("hoaDon") HoaDon hoaDon) {
        hoaDonService.add(hoaDon);
        return "redirect:/hoa-don/hien-thi";
    }

    // update
    @PostMapping("/update/{id}")
    public String update(@PathVariable String id, @ModelAttribute("hoaDon") HoaDon hoaDon) {
        hoaDonService.update(UUID.fromString(id), hoaDon);
        return "redirect:/hoa-don/hien-thi";
    }

    @GetMapping("/tim-kiem")
    public String searchByMa(Model model,
                             @RequestParam(defaultValue = "0", name = "page") Integer pageNum,
                             @RequestParam(name = "ma", required = false) String ma) {
        Page<HoaDon> pagehoaDon;
        if (ma != null && !ma.isEmpty()) {
            pagehoaDon = hoaDonService.searchByMa(ma, pageNum, 5);
        } else {
            pagehoaDon = hoaDonService.phanTrangHoaDon(pageNum, 5);
        }
        model.addAttribute("listHD", pagehoaDon);
        return "admin/HoaDonForm/trangChu";
    }

    @PostMapping("/update/trangthai")
    public String update(@RequestParam("hoadonId") String id, @RequestParam("trangThai") String trangThai, @RequestParam("lyDoHuy") String lyDoHuy) {
        System.out.println("Trạng thái - /update/trangthai : " + trangThai);
        HoaDon hd = hoaDonService.detail(UUID.fromString(id));
        if (trangThai.equals("5")) {
            hd.setTrangThai(trangThai);
            hd.setLyDoHuy(lyDoHuy);

            List<HoaDonChiTiet> hdct = hoaDonCTRepository.findByHoaDon(hd);
            for (HoaDonChiTiet ct : hdct) {
                Optional<ChiTietSanPham> spCTOp = sanPhamCTRepository.findById(ct.getChiTietSanPham().getId());
                ChiTietSanPham ctsp = spCTOp.orElse(null);
                if (ctsp != null) {
                    ctsp.setSoLuong(ctsp.getSoLuong() + ct.getSoLuong());
                    sanPhamCTRepository.save(ctsp);
                }
            }
            //System.out.println(hdct);
            hoaDonService.update(UUID.fromString(id), hd);
        } else {
            // chuyển trạng thái từ chờ xác nhận => chờ lấy hàng = 2
            // cập nhật số lượng sản phẩm trong kho
            if(trangThai.equals("2") && hd.getHinhThucTT().equals("3")){
                // cập nhật thông tin sản phẩm
                List<HoaDonChiTiet> dsHoaDonChiTiet = new ArrayList<>();
                dsHoaDonChiTiet = hoaDonCTService.layDanhSachHoaDonChiTiet_theoIdHoaDon(hd.getId());
                if(dsHoaDonChiTiet.size()>0&& dsHoaDonChiTiet!=null){
                    for(HoaDonChiTiet hoaDonChiTiet : dsHoaDonChiTiet){
                        chiTietSPService.capNhatSoLuongSauKhiDatHang(hoaDonChiTiet.getSoLuong(), hoaDonChiTiet.getChiTietSanPham().getId());
                    }
                }
            }

            hd.setTrangThai(trangThai);
            hoaDonService.update(UUID.fromString(id), hd);

            // cap nhat ca trang thai cua hoa don chi tiet
            hoaDonCTService.capNhatTrangThaiHDCT(hd.getId(),trangThai);
        }

        return "redirect:/hoa-don/hien-thi";
    }

    @PostMapping("/kiemTraSLTonKho_truocChuyenTrangThai")
    public ResponseEntity<Map<String, Object>> kiemTraSLTonKho_truocChuyenTrangThai(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody HoaDonCheckSoLuongTon hoaDonCheckSoLuongTon
    ) throws IOException {
            UUID idHoaDonCheck = hoaDonCheckSoLuongTon.getIdHoaDon();
            boolean isCheckSoLuongKhoTonThieuBan = false;

            List<HoaDonChiTiet> ds_hoaDonChiTiet = new ArrayList<>();
            ds_hoaDonChiTiet = hoaDonCTService.layDanhSachHoaDonChiTiet_theoIdHoaDon(idHoaDonCheck);

            List<ChiTietSanPham> ds_sanPhamKhoKhongDuSP = new ArrayList<>();

            if(ds_hoaDonChiTiet.size()>0 && ds_hoaDonChiTiet!=null){
                for(HoaDonChiTiet dsHoaDonCT : ds_hoaDonChiTiet){
                    // kiểm tra số lượng mua với số lượng trong kho
                    ChiTietSanPham chiTietSanPhamKho = chiTietSPService.chiTietTheoId(dsHoaDonCT.getChiTietSanPham().getId());
                    int soLuongKho = chiTietSanPhamKho.getSoLuong();
                    int soLuongHoaDonCT = dsHoaDonCT.getSoLuong();
                    if(soLuongHoaDonCT>soLuongKho){
                        isCheckSoLuongKhoTonThieuBan = true;
                        ds_sanPhamKhoKhongDuSP.add(chiTietSanPhamKho);
                    }else{
                        isCheckSoLuongKhoTonThieuBan = false;
                    }
                }
            }

            // lưu danh sách sản phẩm ko hợp lệ

            Map<String, Object> jsonResult = new HashMap<String, Object>();
            jsonResult.put("code", 200);
            jsonResult.put("status", "Success");
            jsonResult.put("isCheckSoLuongKhoTonThieuBan", isCheckSoLuongKhoTonThieuBan);
            jsonResult.put("danhSachSP_khoKhongDuSLBan",ds_sanPhamKhoKhongDuSP);

        return ResponseEntity.ok(jsonResult);
    }

}