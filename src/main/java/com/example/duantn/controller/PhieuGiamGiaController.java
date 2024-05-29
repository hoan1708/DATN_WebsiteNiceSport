package com.example.duantn.controller;

import com.example.duantn.dto.Constant;
import com.example.duantn.model.PhieuGiamGia;
import com.example.duantn.repository.PhieuGiamGiaRepository;
import com.example.duantn.request.PhieuGiamGiaRequest;
import com.example.duantn.service.PhieuGiamGiaService;
import com.example.duantn.service.impl.PhieuGiamGiaServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/phieu-giam-gia")
public class PhieuGiamGiaController {
    @Autowired
    PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    PhieuGiamGiaService phieuGiamGiaService;

    @Autowired
    PhieuGiamGiaServiceImpl rep_phieugiamgia;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    //    hienthi
    @GetMapping("/hien-thi")
    public String getAll(Model model,
                         @RequestParam(value = "page",defaultValue = "0")int currentPage
            , HttpServletRequest request){


        model.addAttribute("phieuGiam",new PhieuGiamGia());
        String textSearch = request.getParameter("textsearch");
        //phanTrang
        Pageable pageable = PageRequest.of(currentPage, Constant.pageNumber);
        Page<PhieuGiamGia >phieuGiamGiaPage = rep_phieugiamgia.layDanhSach(textSearch,pageable);
        List<PhieuGiamGia> phieuGiamGias = phieuGiamGiaPage.getContent();

        //muon hien thi so trang
        model.addAttribute("tongSL", phieuGiamGiaPage.getNumberOfElements());
        model.addAttribute("totalPage", phieuGiamGiaPage.getTotalPages());
        model.addAttribute("listPG",phieuGiamGias);
        model.addAttribute("pageChoosedNumber",currentPage);
        return "/admin/PhieuGiamGiaForm/create";
    }
    //    @GetMapping("/hien-thi")
//    public String getAll(Model model,
//                         @RequestParam(value = "page",defaultValue = "0")int page){
//        Integer  size = 5;
//        Pageable pageable = PageRequest.of(page,size);
//        model.addAttribute("phieuGiam",new PhieuGiamGia());
//        model.addAttribute("listPG",phieuGiamGiaRepository.findAll(pageable).getContent());
//        model.addAttribute("totalPage",phieuGiamGiaRepository.findAll(pageable).getTotalElements());
//        return "/admin/PhieuGiamGiaForm/create";
//    }
    //detail
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable String id,Model model){
        PhieuGiamGia phieuGiamGia = phieuGiamGiaService.detail(UUID.fromString(id));
        model.addAttribute("phieuGiam",phieuGiamGia);
        return "admin/PhieuGiamGiaForm/update";
    }
    //delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id){
        phieuGiamGiaService.delete(UUID.fromString(id));
        return "redirect:/phieu-giam-gia/hien-thi";
    }
    //add
//    @PostMapping("/add")
//    public String add(@Validated @ModelAttribute("phieuGiam")PhieuGiamGia phieuGiamGia){
//        phieuGiamGiaService.add(phieuGiamGia);
//        return "redirect:/phieu-giam-gia/hien-thi";
//    }
    //update
    @GetMapping("/view-sua/{id}")
    public String view_sua(
            @PathVariable("id") UUID id,
            @ModelAttribute("phieuGiam") PhieuGiamGiaRequest request,
            Model model
    ) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaService.chiTietTheoId(id);
        model.addAttribute("phieuGiam", phieuGiamGia);
        return "/admin/PhieuGiamGiaForm/update";
    }

    @PostMapping("/sua/{id}")
    public String sua(
            @PathVariable("id") UUID id,
            @Valid @ModelAttribute("phieuGiam") PhieuGiamGiaRequest phieuGiamGiaRequestrequest,
            BindingResult result,
            HttpServletRequest request,
            Model model
    ) throws ParseException {
        if (result.hasErrors()) {
            return "/admin/PhieuGiamGiaForm/update";
        }

        PhieuGiamGia phieuGiamGiaCu = rep_phieugiamgia.chiTietTheoId(id);

        if (rep_phieugiamgia.kiemTraTrungMaKhong(phieuGiamGiaRequestrequest.getMa(), phieuGiamGiaCu.getMa())) {
            model.addAttribute("trungMa", true);
            return "/admin/PhieuGiamGiaForm/update";
        }

        if (rep_phieugiamgia.kiemTraTrungTenKhong(phieuGiamGiaRequestrequest.getTenPhieu(), phieuGiamGiaCu.getTenPhieu())) {
            model.addAttribute("trungTen", true);
            return "/admin/PhieuGiamGiaForm/update";
        }

        PhieuGiamGia phieuGiamUpdate = new PhieuGiamGia();
        phieuGiamUpdate.setId(phieuGiamGiaCu.getId());
        phieuGiamUpdate.setMa(phieuGiamGiaRequestrequest.getMa());
        phieuGiamUpdate.setTenPhieu(phieuGiamGiaRequestrequest.getTenPhieu());
        phieuGiamUpdate.setGiaTriGiam(phieuGiamGiaRequestrequest.getGiaTriGiam());
        phieuGiamUpdate.setGiaTienXetDieuKien(phieuGiamGiaRequestrequest.getGiaTienXetDieuKien());
        phieuGiamUpdate.setHinhThucGiam(phieuGiamGiaRequestrequest.getHinhThucGiam());

        Integer soLuong = phieuGiamGiaRequestrequest.getSoLuong();
        phieuGiamUpdate.setSoLuong(soLuong);

        phieuGiamUpdate.setNgayBatDauApDung(phieuGiamGiaRequestrequest.getNgayBatDauApDung());
        phieuGiamUpdate.setNgayKetThucApDung(phieuGiamGiaRequestrequest.getNgayKetThucApDung());

        if (phieuGiamUpdate.getNgayKetThucApDung() != null) {
            if (Constant.getDateNowByTime().before(phieuGiamGiaRequestrequest.getNgayBatDauApDung())) {
                phieuGiamUpdate.setTrangThai(2);
            } else if (Constant.getDateNowByTime().before(phieuGiamGiaRequestrequest.getNgayKetThucApDung())) {
                phieuGiamUpdate.setTrangThai(1);
            } else {
                phieuGiamUpdate.setTrangThai(0);
            }
        } else {
            phieuGiamUpdate.setTrangThai(1);
        }

//        Integer trangThai = phieuGiamGiaRequestrequest.getTrangThai();
//        phieuGiamUpdate.setTrangThai(trangThai);
        phieuGiamUpdate.setNguoiTao(1);
        phieuGiamUpdate.setNguoiSua(1);
        phieuGiamUpdate.setNgayTao(phieuGiamGiaCu.getNgayTao());
        phieuGiamUpdate.setNgaySua(Constant.getDateNow());


        rep_phieugiamgia.capNhat(phieuGiamUpdate);
        model.addAttribute("phieuGiam", phieuGiamGiaRequestrequest);
        model.addAttribute("messageSuccess", true);

        return "redirect:/phieu-giam-gia/hien-thi";
    }
    @PostMapping("/them")
    public String themMoi(Model model,
                          @Valid @ModelAttribute("phieuGiam") PhieuGiamGiaRequest request,
                          BindingResult result,
                          @RequestParam(name = "page", defaultValue = "0")
                                  int currentPage,
                          HttpServletRequest httpServletRequest)
            throws ParseException {
        if (result.hasErrors()) {
            String textSearch = httpServletRequest.getParameter("textsearch");

            // phan trang
            Pageable pageable = PageRequest.of(currentPage, Constant.pageNumber);
            Page<PhieuGiamGia> pagePhieuGiam = rep_phieugiamgia.layDanhSach(textSearch, pageable);
            List<PhieuGiamGia> phieuGiamGias = pagePhieuGiam.getContent();

            // muon hien thi so trang
            model.addAttribute("tongSL", pagePhieuGiam.getNumberOfElements());
            model.addAttribute("totalPage", pagePhieuGiam.getTotalPages());
            model.addAttribute("listPG", phieuGiamGias);
            model.addAttribute("pageChoosedNumber", currentPage);
            return "/admin/PhieuGiamGiaForm/create";
        }


        if (rep_phieugiamgia.kiemTraTrungMaKhong(request.getMa())) {
//            System.out.println("Trùng mã rồi ...");
            model.addAttribute("trungMa", true);

            String textSearch = httpServletRequest.getParameter("textsearch");

            // phan trang
            Pageable pageable = PageRequest.of(currentPage, Constant.pageNumber);
            Page<PhieuGiamGia> pagePhieuGiam = rep_phieugiamgia.layDanhSach(textSearch, pageable);
            List<PhieuGiamGia> phieuGiamGias = pagePhieuGiam.getContent();

            // muon hien thi so trang
            model.addAttribute("tongSL", pagePhieuGiam.getNumberOfElements());
            model.addAttribute("totalPage", pagePhieuGiam.getTotalPages());
            model.addAttribute("listPG", phieuGiamGias);
            model.addAttribute("pageChoosedNumber", currentPage);
            return "/admin/PhieuGiamGiaForm/create";
        }

        if (rep_phieugiamgia.kiemTraTrungTenKhong(String.valueOf(request.getTenPhieu()))) {
            model.addAttribute("trungTen", true);

            String textSearch = httpServletRequest.getParameter("textsearch");

            // phan trang
            Pageable pageable = PageRequest.of(currentPage, Constant.pageNumber);
            Page<PhieuGiamGia> pagePhieuGiam = rep_phieugiamgia.layDanhSach(textSearch, pageable);
            List<PhieuGiamGia> phieuGiamGias = pagePhieuGiam.getContent();

            // muon hien thi so trang
            model.addAttribute("tongSL", pagePhieuGiam.getNumberOfElements());
            model.addAttribute("totalPage", pagePhieuGiam.getTotalPages());
            model.addAttribute("listPG", phieuGiamGias);
            model.addAttribute("pageChoosedNumber", currentPage);
            return "/admin/PhieuGiamGiaForm/create";
        }
        PhieuGiamGia phieuGiamGiaThemMoi = new PhieuGiamGia();
        phieuGiamGiaThemMoi.setId(null);
        phieuGiamGiaThemMoi.setTenPhieu(request.getTenPhieu());
        phieuGiamGiaThemMoi.setMa(request.getMa());
        phieuGiamGiaThemMoi.setGiaTriGiam(request.getGiaTriGiam());
        phieuGiamGiaThemMoi.setGiaTienXetDieuKien(request.getGiaTienXetDieuKien());
        phieuGiamGiaThemMoi.setHinhThucGiam(request.getHinhThucGiam());
//        phieuGiamGiaThemMoi.setNgayBatDauApDung(request.getNgayBatDauApDung());
//        phieuGiamGiaThemMoi.setNgayKetThucApDung(request.getNgayKetThucApDung());
        Integer soLuong = request.getSoLuong();
        phieuGiamGiaThemMoi.setSoLuong(soLuong);

        phieuGiamGiaThemMoi.setNgayBatDauApDung(request.getNgayBatDauApDung());
        phieuGiamGiaThemMoi.setNgayKetThucApDung(request.getNgayKetThucApDung());


        if (Constant.getDateNowByTime().before(request.getNgayBatDauApDung())) {
            phieuGiamGiaThemMoi.setTrangThai(2);
        } else if (Constant.getDateNowByTime().before(request.getNgayKetThucApDung())) {
            phieuGiamGiaThemMoi.setTrangThai(1);
        } else {
            phieuGiamGiaThemMoi.setTrangThai(0);
        }

        phieuGiamGiaThemMoi.setNgayTao(Constant.getDateNow());
        phieuGiamGiaThemMoi.setNgaySua(Constant.getDateNow());
        phieuGiamGiaThemMoi.setNguoiTao(1);
        phieuGiamGiaThemMoi.setNguoiSua(1);

        rep_phieugiamgia.add(phieuGiamGiaThemMoi);
        model.addAttribute("phieuGiam", request);
        model.addAttribute("messageSuccess", true);

        String textSearch = httpServletRequest.getParameter("textsearch");

        // phan trang
        Pageable pageable = PageRequest.of(currentPage, Constant.pageNumber);
        Page<PhieuGiamGia> pagePhieuGiam = rep_phieugiamgia.layDanhSach(textSearch, pageable);
        List<PhieuGiamGia> phieuGiamGias = pagePhieuGiam.getContent();

        // muon hien thi so trang
        model.addAttribute("tongSL", pagePhieuGiam.getNumberOfElements());
        model.addAttribute("totalPage", pagePhieuGiam.getTotalPages());
        model.addAttribute("listPG", phieuGiamGias);
        model.addAttribute("pageChoosedNumber", currentPage);
        return "redirect:/phieu-giam-gia/hien-thi";

    }
}
