package com.example.duantn.controller.customer;

import com.example.duantn.model.*;
import com.example.duantn.request.*;
import com.example.duantn.service.impl.*;
import com.example.duantn.dto.Constant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/gio-hang")    //   /gio-hang/view-thanhToan
public class CartController {

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    HttpSession session;

    @Autowired
    ChiTietSPServiceImpl chiTietSPService;

    @Autowired
    HoaDonServiceImpl hoaDonService;

    @Autowired
    HoaDonCTServiceImpl hoaDonChiTietService;

    @Autowired
    LoaiSanPhamServiceImpl loaiSanPhamService;

    @GetMapping("/view-gio")
    public String viewGioHang(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        HttpSession session = request.getSession();
        GioHang cartThanhToan = new GioHang();
        session.setAttribute("cartThanhToan", cartThanhToan);
        // lấy giỏ hàng
        Double totalCartPrice = 0.0;

        GioHang cart = null;

        String errorMessage = null;
        String message = null;

        if (session.getAttribute("cart") != null) {
            cart = (GioHang) session.getAttribute("cart");
            List<SanPhamTrongGioHang> dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();
            totalCartPrice = dsSanPhamTrongGio.stream().mapToDouble(x ->x.getGia()).sum();

            Integer tongSLSP_trongGio = 0;
            for (SanPhamTrongGioHang sanPham : dsSanPhamTrongGio) {
                tongSLSP_trongGio += sanPham.getSoLuong();
            }

            model.addAttribute("tongSoLuongSanPhamTrongGio",tongSLSP_trongGio);
            model.addAttribute("totalCartPrice", totalCartPrice);
            model.addAttribute("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());

//				model.addAttribute("cartProducts", cart.getDs_cartproduct());

            message = "Có " + cart.totalSanPhamTrongGioHangs() + " sản phẩm";
//            System.out.println("Có sản phẩm trong giỏ");
        } else {
            // trường hợp: chưa có trong giỏ hàng
            cart = new GioHang();
            errorMessage = "Không có sản phẩm nào trong giỏ hàng ";

//            System.out.println("Không có sản phẩm nào trong giỏ hàng");
        }

        List<SanPhamTrongGioHang> dsSanPhamTrongGio = new ArrayList<>();
        dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();

        Integer tongSLSP_trongGio = 0;
        for (SanPhamTrongGioHang sanPham : dsSanPhamTrongGio) {
            tongSLSP_trongGio += sanPham.getSoLuong();
        }

        System.out.println("Tổng số lượng sản phẩm trong giỏ : " + tongSLSP_trongGio);

        model.addAttribute("sanPhamTrongGio", dsSanPhamTrongGio);
        model.addAttribute("tongSoLuongSanPhamTrongGio", tongSLSP_trongGio);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("message", message);

        return "customer/gioHang/view_gio_hang";
    }

    @GetMapping("/view-ketQuaThanhToan")
    public String ketQuaThanhToan(
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        HttpSession session = request.getSession();  // tạo mới 1 session
        HoaDon hoaDonVuaThanhToan = new HoaDon();
        HoaDon hoaDonSession = new HoaDon();
        String errorMessage = null;

        if (session.getAttribute("hoaDonVuaDatOnline") != null) {
            hoaDonSession = (HoaDon) session.getAttribute("hoaDonVuaDatOnline");
        } else {
            // trường hợp: chưa có trong giỏ hàng
            HoaDon hoaDonNew = new HoaDon();
            session.setAttribute("hoaDonVuaDatOnline", hoaDonNew);
            errorMessage = "Đặt hàng thất bại";
        }

        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("hoaDonVuaThanhToan", hoaDonSession);

        return "customer/gioHang/ketQuaThanhToan";
    }

    @GetMapping("/view-traThongTinDonHang")
    public String traThongTinDonHang(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name = "searchByMaHoaDon", defaultValue = "") String searchBy_maHoaDon,
            @ModelAttribute("hoaDonDetail") HoaDon hoaDon,
            Model model
    ) {
        List<HoaDonChiTiet> danhSach_hoaDonChiTiet = new ArrayList<>();
        // truyền mã hóa đơn => nhận được kết quả là 1 hóa đơn duy nhất
        // từ mã hóa đơn duy nhất => set lên form
        if(searchBy_maHoaDon==null || searchBy_maHoaDon.equals("")){
            model.addAttribute("danhSach_sanPhamTrongHoaDonCT",danhSach_hoaDonChiTiet);
            model.addAttribute("hoaDonDetail",new HoaDon());
        }else {
            String maHoaDonSearch = searchBy_maHoaDon.trim();
            HoaDon hoaDonSearch = hoaDonService.layHoaDonTheoMa(maHoaDonSearch); // thông tin hóa đơn cơ bản

            // danh sách sản phẩm trong hóa đơn đó ?

            if(hoaDonSearch!=null){
                danhSach_hoaDonChiTiet = hoaDonChiTietService.layDanhSachHoaDonChiTiet_theoIdHoaDon(hoaDonSearch.getId());
            }

            model.addAttribute("danhSach_sanPhamTrongHoaDonCT",danhSach_hoaDonChiTiet);
            model.addAttribute("hoaDonDetail",hoaDonSearch);
        }
        return "customer/gioHang/traCuuDonHang";
    }

    @GetMapping("/view-thanhToan")
    public String viewThanhToan(
            HttpServletRequest request,
            Model model
    ) {
        model.addAttribute("datHang", new HoaDonRequest());
        HttpSession session = request.getSession();

        GioHang cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
        GioHang gioHangKH = (GioHang) session.getAttribute("cart");
        List<SanPhamTrongGioHang> dsSanPhamTrongGio = new ArrayList<>();

        //  nếu trong view thanh toán có sản phẩm => set danh sách giỏ theo - cartThanhToan
        if (cartThanhToan!=null && cartThanhToan.getDs_SanPhamTrongGioHang().size()>0) {
            dsSanPhamTrongGio = cartThanhToan.getDs_SanPhamTrongGioHang();
        } else {
            if (gioHangKH != null) {
                dsSanPhamTrongGio = gioHangKH.getDs_SanPhamTrongGioHang();

                if (cartThanhToan == null) {
                    cartThanhToan = new GioHang();
                }
                cartThanhToan.setDs_SanPhamTrongGioHang(dsSanPhamTrongGio);
                session.setAttribute("cartThanhToan", cartThanhToan);
            }
        }

        System.out.println("Size ds sản phẩm trong giỏ - tại view thanh toán : " + dsSanPhamTrongGio.size());

        Double tongTienDonHang = dsSanPhamTrongGio.stream().mapToDouble(sp -> sp.getGia()).sum();

        Integer tongCanNangDonHang = 0;
        if(dsSanPhamTrongGio.size()>0){
            for(SanPhamTrongGioHang sp_gio : dsSanPhamTrongGio){
                int trongLuongInt = (int) Math.round(sp_gio.getTrongLuong());
                tongCanNangDonHang += trongLuongInt * sp_gio.getSoLuong();
            }
        }
        System.out.println("Tổng cân nặng đơn hàng : "  + tongCanNangDonHang);

        model.addAttribute("dsSanPham", dsSanPhamTrongGio);
        model.addAttribute("tongTienDonHang", tongTienDonHang);
        model.addAttribute("trongLuongDonHang", tongCanNangDonHang);

        return "customer/gioHang/thanh_toan";
    }

    public void demo() throws Exception {
        URL url = new URL("https://vapi.vnappmob.com/api/province/");
        URLConnection urlConnection = url.openConnection();
        Scanner scanner = new Scanner(urlConnection.getInputStream());
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNext()) {
            sb.append(scanner.nextLine());
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.readValue(sb.toString().getBytes(StandardCharsets.UTF_8), Object.class);
    }

    @PostMapping("/dat-hang")
    public String datHang(
            HttpServletRequest request,
            @Valid @ModelAttribute("datHang") HoaDonRequest datHangRequest,
            BindingResult result,
            Model model
    ) throws ParseException {
        HttpSession session = request.getSession();  // tạo mới 1 session
        HoaDon hoaDonVuaThanhToan = new HoaDon();
        session.setAttribute("hoaDonVuaDatOnline", hoaDonVuaThanhToan);

        if (result.hasErrors()) {
            System.out.println("Có lỗi khi đặt hàng ");
            System.out.println("Lỗi khi đặt hàng : " + result.toString());

            GioHang cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
            List<SanPhamTrongGioHang> dsSanPhamTrongGio = new ArrayList<>();
            if (cartThanhToan != null) {
                dsSanPhamTrongGio = cartThanhToan.getDs_SanPhamTrongGioHang();
            }
            Double tongTienDonHang = dsSanPhamTrongGio.stream().mapToDouble(sp -> sp.getGia()).sum();
//        System.out.println("Tổng tiền đơn hàng " +  tongTienDonHang);
            model.addAttribute("dsSanPham", dsSanPhamTrongGio);
            model.addAttribute("tongTienDonHang", tongTienDonHang);
            return "customer/gioHang/thanh_toan";
        }

        // lấy ra danh sách giỏ hàng thanh toán
        GioHang gioHangThanhToan = (GioHang) session.getAttribute("cartThanhToan");
        HoaDon hoaDon = new HoaDon();

        // tạo mã hóa đơn random
        int randomInt = (int) (Math.random() * 100);
//        String maHoaDon = String.valueOf(randomInt);
        hoaDon.setTenKH(datHangRequest.getHoVaTen());
        hoaDon.setDiaChi(datHangRequest.getDiaChi());
        hoaDon.setGhiChu(datHangRequest.getGhiChu());
        hoaDon.setSoDT(datHangRequest.getSoDT());
        hoaDon.setEmail(datHangRequest.getEmail());
        hoaDon.setNgayMua(Constant.getDateNow());
        hoaDon.setTinh_thanhPho(datHangRequest.getTinh_thanhPho());
        hoaDon.setQuan_huyen(datHangRequest.getQuan_huyen());
        hoaDon.setPhuong_xa(datHangRequest.getPhuong_xa());
        hoaDon.setHinhThucTT(datHangRequest.getHinhThucThanhToan());
        hoaDon.setLoaiHoaDon("3");
        hoaDon.setNgayTao(Constant.getDateNowByTime());
        hoaDon.setNgaySua(Constant.getDateNowByTime());
//        hoaDon.setMa("HD-" + maHoaDon);
        System.out.println("Phí ship : " + datHangRequest.getPhiShip()*1000);
        GioHang cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
        List<SanPhamTrongGioHang> dsSanPhamTrongGio = new ArrayList<>();
        if (cartThanhToan != null) {
            dsSanPhamTrongGio = cartThanhToan.getDs_SanPhamTrongGioHang();
        }
        Double tongTienDonHang = dsSanPhamTrongGio.stream().mapToDouble(sp -> sp.getGia() ).sum();

        System.out.println("Tổng tiền thanh toán : " + (tongTienDonHang + datHangRequest.getPhiShip() * 1000));
        Double tongTienThanhToan = tongTienDonHang  + datHangRequest.getPhiShip() * 1000;
        hoaDon.setTienVanChuyen(datHangRequest.getPhiShip() * 1000);
        hoaDon.setTongTienThanhToan(tongTienThanhToan);

        hoaDon.setTrangThai("1");

        // thêm thông tin đơn hàng vào hóa đơn - trong database
        HoaDon hoaDon1 = hoaDonService.themMoi(hoaDon);

        UUID idHoaDonVuaTao = hoaDon1.getId();
        String idHoaDonString = idHoaDonVuaTao.toString();
        String kyTuCuoi_id = idHoaDonString.substring(idHoaDonString.length() - 4);
        String timeSystemStr = Constant.getDate_TimeHeThongCurrent();
        String maHoaDonNew = "HD-"+timeSystemStr+kyTuCuoi_id;

        hoaDon1.setMa(maHoaDonNew);
        hoaDonService.capNhat(hoaDon1);

        session.setAttribute("hoaDonVuaDatOnline", hoaDon1);

        UUID id_hoaDon = hoaDon1.getId();

//        System.out.println("Thêm mới hóa đơn thành công ");

        // lấy hóa đơn ra theo mã hóa đơn phía trên => thêm sản phẩm vào hóa đơn chi tiết
//        HoaDon hoaDon1 = hoaDonService.layHoaDonTheoMa(maHoaDon);

        // lấy ra danh sách sản phẩm được thêm vào cart thanh toán
        List<SanPhamTrongGioHang> dsSanPhamTrongGioThanhToan = gioHangThanhToan.getDs_SanPhamTrongGioHang();


        // duyệt list sau đó thêm sản phẩm chi tiết vào hóa đơn chi tiết
        for (SanPhamTrongGioHang sanPhamGio : dsSanPhamTrongGioThanhToan) {
            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
            // chi tiet san pham
            ChiTietSanPham chiTietSanPham = new ChiTietSanPham();
            chiTietSanPham = chiTietSPService.chiTietTheoId(sanPhamGio.getIdSanPhamCT());

            hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
            // hóa đơn
            HoaDon hoaDon2 = new HoaDon();
            hoaDon2.setId(id_hoaDon);
            hoaDonChiTiet.setHoaDon(hoaDon2);

            Double giaGiam = 0.0;
            if(chiTietSanPham.getGiaTriGiam()!=null){
                giaGiam = chiTietSanPham.getGiaTriGiam()<=0||chiTietSanPham.getGiaTriGiam()==null?0:chiTietSanPham.getGiaTriGiam();

            }

            hoaDonChiTiet.setGiaSauGiam(giaGiam);
            hoaDonChiTiet.setGiaGocSP(chiTietSanPham.getGiaTriSanPham());

            hoaDonChiTiet.setDonGia(sanPhamGio.getGia());
            hoaDonChiTiet.setSoLuong(sanPhamGio.getSoLuong());
            hoaDonChiTiet.setNgayTao(Constant.getDateNow());
            hoaDonChiTiet.setTrangThai(1);

            // them sản phẩm vào hóa đơn chi tiết
            hoaDonChiTietService.themMoi(hoaDonChiTiet);
            // cập nhật số lượng sản phẩm trong DB
//            chiTietSPService.capNhatSoLuongSauKhiDatHang(sanPhamGio.getSoLuong(), sanPhamGio.getIdSanPhamCT());
        }

        // lấy ra danh sách giỏ hàng thanh toán
        GioHang gioHangCuaKhach = (GioHang) session.getAttribute("cart");
        GioHang gioHangCartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
        List<SanPhamTrongGioHang> dsSanPhamGioHang = gioHangCuaKhach.getDs_SanPhamTrongGioHang();
        List<SanPhamTrongGioHang> dsSanPhamGioHangThanhToan = gioHangCartThanhToan.getDs_SanPhamTrongGioHang();

        System.out.println("Danh sách sản phẩm trong cart thanh toán : " + dsSanPhamGioHangThanhToan.size());
        // xóa các element trùng của danh sách 2 trong danh sách 1
        dsSanPhamGioHang.removeAll(dsSanPhamGioHangThanhToan);

        for(int i=0; i<dsSanPhamGioHang.size();i++){
            for(int j=0;j<dsSanPhamGioHangThanhToan.size();j++){
                if(dsSanPhamGioHang.get(i).getIdSanPhamCT().equals(dsSanPhamGioHangThanhToan.get(j).getIdSanPhamCT())){
                    dsSanPhamGioHang.remove(i);
                    continue;
                }
            }
        }

        System.out.println("Danh sách giỏ hàng sau xóa : " + dsSanPhamGioHang.size());

        // xóa hết cart thanh toán => vì đã hoàn thành đặt hàng rồi
        GioHang capNhatGioHangKhachHang = new GioHang();
        if (dsSanPhamGioHang.size() > 0) {
            for (SanPhamTrongGioHang sanPhamGio : dsSanPhamGioHang) {
                capNhatGioHangKhachHang.getDs_SanPhamTrongGioHang().add(sanPhamGio);
            }
        }

        System.out.println("Danh sách giỏ hàng sau đặt : " + dsSanPhamGioHang.size());

        session.setAttribute("cart", capNhatGioHangKhachHang);
        session.setAttribute("cartThanhToan", new GioHang());

        int tongSoLuongTrongGio = dsSanPhamGioHang.stream().mapToInt(SanPhamTrongGioHang::getSoLuong).sum();

        session.setAttribute("totalCartProducts", tongSoLuongTrongGio);

        // hết lỗi trên form thanh toán => check tiếp hình thức thanh toán

        if(datHangRequest.getHinhThucThanhToan().equals("2")) {  // vnpay
            emailSenderService.sendSimpleEmail(hoaDon1.getEmail(), "Đơn hàng đặt thành công tại NiceSport và đang trong quá trình bạn thực hiện thanh toán vnpay", "Bạn có thể dùng mã hóa đơn để tra cứu tình trạng đơn hàng: " + maHoaDonNew
                    + "\nClick vào link để tra cứu đơn hàng vừa đặt:" + "http://localhost:8080/gio-hang/view-traThongTinDonHang?searchByMaHoaDon=" + maHoaDonNew
                    + "\nClick tại đây để tiếp tục mua hàng: " + "http://localhost:8080/gio-hang/view-trangChu2"
                    + "\n\n Cảm ơn bạn đã tin tưởng và đồng hành cùng Nice Sport!");
        }else {
            emailSenderService.sendSimpleEmail(hoaDon1.getEmail(), "Đơn hàng đặt thành công tại NiceSport", "" +
                    "\n Đơn hàng: thanh toán khi nhận hàng \nVui lòng kiểm tra kỹ hàng và thanh toán đơn hàng! " +
                    "\nBạn có thể dùng mã hóa đơn để tra cứu tình trạng đơn hàng: " + maHoaDonNew
                    + "\nClick vào link để tra cứu đơn hàng vừa đặt:" + "http://localhost:8080/gio-hang/view-traThongTinDonHang?searchByMaHoaDon=" + maHoaDonNew
                    + "\nClick tại đây để tiếp tục mua hàng: " + "http://localhost:8080/gio-hang/view-trangChu2"
                    + "\n\n Cảm ơn bạn đã tin tưởng và đồng hành cùng Nice Sport!");
        }

        String hinhThucThanhToan = request.getParameter("hinhThucThanhToan");
//        System.out.println("Hình thức thanh toán" + hinhThucThanhToan);
        if (hinhThucThanhToan.equals("2")) {
            session.setAttribute("ThongTinDatHang", datHangRequest);
            return "redirect:/apivnpay/create";
        }


//        System.out.println("Đặt hàng thành công");

        return "redirect:/gio-hang/view-ketQuaThanhToan";
    }

    @GetMapping("/view-trangChu2")
    public String viewTrangChu2(
            Model model,
            HttpServletRequest httpServletRequest
    ) {
//        HttpSession session = httpServletRequest.getSession();
//        GioHang cartThanhToan = new GioHang();
//        session.setAttribute("cartThanhToan", cartThanhToan);

        // lấy danh sách chi tiết sản phẩm
        List<ChiTietSanPham> dsSanPhamCT = chiTietSPService.layDanhSachSanPham_soLuongLonHon_0(Pageable.unpaged());

        // lấy danh sách UUID sản phẩm
        List<UUID> ds_uuidSanPham_tuSPCT = new ArrayList<>();
        ds_uuidSanPham_tuSPCT = chiTietSPService.layDanhSach_IdSanPham_trongSanPhamCT();

        // tạo 1 danh sách sản phẩm mới => lưu danh sách sản phẩm chi tiết sẽ hiển thị lên trang chủ
        List<ChiTietSanPham> dsSanPhamCT_new = new ArrayList<>();
        for (UUID idSanPham : ds_uuidSanPham_tuSPCT) {
            for (ChiTietSanPham sanPhamCT : dsSanPhamCT) {
                if (idSanPham.equals(sanPhamCT.getSanPham().getId())) {
                    dsSanPhamCT_new.add(sanPhamCT);
                    break;
                }
            }
        }
//        List<LoaiSanPham> dsLoaiSPTrongSanPhamCT = new ArrayList<>();
//        dsLoaiSPTrongSanPhamCT = chiTietSPService.layTatCa_idLoaiSP_distinct_trongChiTietSP();

//        System.out.println("Size of dsSanPham : " + dsSanPhamCT.size());
        model.addAttribute("dsSanPham", dsSanPhamCT_new);
//        model.addAttribute("dsLoaiSP_trongSanPhamCT", dsLoaiSPTrongSanPhamCT);

        session = httpServletRequest.getSession();
        Integer soLuongSanPhamTrongGio = (Integer) session.getAttribute("totalCartProducts") == null ? 0 : (Integer) session.getAttribute("totalCartProducts");
//        System.out.println("Số lượng sản phẩm trong giỏ : " + soLuongSanPhamTrongGio);
        model.addAttribute("totalCartProducts", soLuongSanPhamTrongGio);

        return "customer/gioHang/trang_chu_daSua";
    }

    @GetMapping("/view-xemToanBoSanPham")
    public String xemToanBoSanPham(
            Model model,
            HttpServletRequest httpServletRequest
    ) {
//        HttpSession session = httpServletRequest.getSession();
//        GioHang cartThanhToan = new GioHang();
//        session.setAttribute("cartThanhToan", cartThanhToan);

        // lấy danh sách chi tiết sản phẩm
        List<ChiTietSanPham> dsSanPhamCT = chiTietSPService.layDanhSachSanPham_soLuongLonHon_0(Pageable.unpaged());

        // lấy danh sách UUID sản phẩm
        List<UUID> ds_uuidSanPham_tuSPCT = new ArrayList<>();
        ds_uuidSanPham_tuSPCT = chiTietSPService.layDanhSach_IdSanPham_trongSanPhamCT();

        // tạo 1 danh sách sản phẩm mới => lưu danh sách sản phẩm chi tiết sẽ hiển thị lên trang chủ
        List<ChiTietSanPham> dsSanPhamCT_new = new ArrayList<>();
        for (UUID idSanPham : ds_uuidSanPham_tuSPCT) {
            for (ChiTietSanPham sanPhamCT : dsSanPhamCT) {
                if (idSanPham.equals(sanPhamCT.getSanPham().getId())) {
                    dsSanPhamCT_new.add(sanPhamCT);
                    break;
                }
            }
        }
//        List<LoaiSanPham> dsLoaiSPTrongSanPhamCT = new ArrayList<>();
//        dsLoaiSPTrongSanPhamCT = chiTietSPService.layTatCa_idLoaiSP_distinct_trongChiTietSP();

//        System.out.println("Size of dsSanPham : " + dsSanPhamCT.size());
        model.addAttribute("dsSanPham", dsSanPhamCT_new);
//        model.addAttribute("dsLoaiSP_trongSanPhamCT", dsLoaiSPTrongSanPhamCT);

        session = httpServletRequest.getSession();
        Integer soLuongSanPhamTrongGio = (Integer) session.getAttribute("totalCartProducts") == null ? 0 : (Integer) session.getAttribute("totalCartProducts");
//        System.out.println("Số lượng sản phẩm trong giỏ : " + soLuongSanPhamTrongGio);
        model.addAttribute("totalCartProducts", soLuongSanPhamTrongGio);

        return "customer/gioHang/xemToanBoSanPham";
    }

    @GetMapping("/getListLoaiSanPham_countNumber")
    public ResponseEntity<?> getListProductDetail(
            Model model
    ) {
        List<LoaiSanPham> danhSach_loaiSanPham = new ArrayList<>();
        danhSach_loaiSanPham = loaiSanPhamService.layDanhSach();

        List<ChiTietSanPham> ds_chiTietSanPham = new ArrayList<>();
        ds_chiTietSanPham = chiTietSPService.layDanhSach();

        List<LoaiSanPhamCount> ds_loaiSanPhamCount = new ArrayList<>();

        // chạy for 1: loại sản phẩm
        // chạy for 2: chi tiết sản phẩm

        // phía trước count 2: set = 0
        // if chi tiết sản phẩm == loại sản phẩm => count ++
        // chạy hết for 2 => được tổng đếm => lưu id loai, số lượng đếm được
        // lưu danh sách loại sản phẩm đếm được vào danh sách
        int count = 0;
        for (LoaiSanPham loaiSanPham : danhSach_loaiSanPham) {
            count = 0;
            for (ChiTietSanPham chiTietSanPham : ds_chiTietSanPham) {
                if (chiTietSanPham.getLoaiSanPham().getId().equals(loaiSanPham.getId())) {
                    count++;
                }
            }
            LoaiSanPhamCount loaiSanPhamCount = new LoaiSanPhamCount();
            loaiSanPhamCount.setId_loaiSP(loaiSanPham.getId());
            loaiSanPhamCount.setSoLuongLoaiSP(count);
            loaiSanPhamCount.setTenLoaiSP(loaiSanPham.getTen());
            ds_loaiSanPhamCount.add(loaiSanPhamCount);
        }

//        System.out.println("List ra danh sách loại sản phẩm count : " + ds_loaiSanPhamCount.size());
//        for(LoaiSanPhamCount loaiSanPhamCount : ds_loaiSanPhamCount){
//            System.out.println(loaiSanPhamCount);
//        }
        return ResponseEntity.of(Optional.ofNullable(ds_loaiSanPhamCount));
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<Map<String, Object>> addToCart(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody SanPhamTrongGioHang sanPhamTrongGioHang
    ) throws IOException {
        chiTietSPService.capNhatGiaTriGiamNull();

        int soLuongMuonMua = sanPhamTrongGioHang.getSoLuongMuaThem();
        int soLuongSPTrongGio = 0;
        boolean conSoLuongTrongKho = true;

        ChiTietSanPham chiTietSanPham = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
        int soLuongSPTrongKho = chiTietSanPham.getSoLuong();
        if(soLuongSPTrongKho<0){
            conSoLuongTrongKho = false;
        }

//        System.out.println("Chạy vào add-to-cart xử lý : " );
        HttpSession session = request.getSession();  // tạo mới 1 session
        GioHang cart = null; // khởi tạo 1 Object cart = null

        // nếu giỏ hàng không null, thì lấy session có tên cart gán vào giỏ hàng
        List<SanPhamTrongGioHang> dsSanPhamGioHangN = new ArrayList<>();
        if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
            cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào

            dsSanPhamGioHangN = cart.getDs_SanPhamTrongGioHang();
            for (SanPhamTrongGioHang sanPhamDuocChon : dsSanPhamGioHangN) {
                if (sanPhamDuocChon.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                    soLuongSPTrongGio = sanPhamDuocChon.getSoLuong();
                    break;
                }
            }
        } else {  // chưa có j thì khởi tạo cart mới
            cart = new GioHang();
            session.setAttribute("cart", cart);
//            System.out.println("giỏ hàng chưa có gì cả ");
        }

        // TH: tổng số lượng mua + tổng số lượng trong giỏ > số lượng trong kho ( số lượng mua - vượt quá số lượng trong kho )
        // đưa ra thông báo số lượng mua vượt quá
        if ((soLuongMuonMua + soLuongSPTrongGio) > soLuongSPTrongKho) {
            int totalCartProducts = dsSanPhamGioHangN.size();

            Map<String, Object> jsonResult = new HashMap<String, Object>();
            jsonResult.put("code", 200);
            jsonResult.put("status", "Success");
            jsonResult.put("soLuongMuaVuotQua", true);


            jsonResult.put("totalCartProducts", totalCartProducts);
            jsonResult.put("soLuongMua", soLuongMuonMua);
            jsonResult.put("soLuongSPTrongKho", soLuongSPTrongKho);
            jsonResult.put("soLuongCuaSanPhamChon_trongGioDaThem", soLuongSPTrongGio);
            jsonResult.put("conSoLuongTrongKho", conSoLuongTrongKho);

            return ResponseEntity.ok(jsonResult);
        }

        // lấy ra thông tin sản phẩm chi tiết từ DB
        ChiTietSanPham dbSanPhamCT = new ChiTietSanPham();
        if (sanPhamTrongGioHang.getIdSanPhamCT() != null) {
            dbSanPhamCT = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
        }

        boolean isExisProduct = false;

        // gán danh sách sản phẩm trong giỏ hàng = dsSanPhamTrongGio
        List<SanPhamTrongGioHang> dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();

        // nếu sản phẩm có trong giỏ hàng rồi thì sửa số lượng
        for (SanPhamTrongGioHang spTrongGioHang : dsSanPhamTrongGio) {
            // tìm thấy sản phẩm trong giỏ : cộng dồn số lượng
            if (spTrongGioHang.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                isExisProduct = true;
                Integer soLuongMoi = spTrongGioHang.getSoLuong() + sanPhamTrongGioHang.getSoLuongMuaThem();
                spTrongGioHang.setSoLuong(soLuongMoi);
                // cập nhật số lượng => cập nhật thành tiền => tổng tiền thanh toán = tổng tất cả thành tiền
                // muốn tính được giá => lấy được giá bán sản phẩm
                // giá bán sản phẩm lại có 2 trường hợp
                // lấy giá bán sản phẩm tại đâu => trong DB
                Double giaBan = 0.0;
                if(dbSanPhamCT.getGiaTriGiam()>0 && dbSanPhamCT.getGiaTriGiam()!=null){
                    giaBan = dbSanPhamCT.getGiaTriGiam();
                }else {
                    giaBan = dbSanPhamCT.getGiaTriSanPham();
                }
                spTrongGioHang.setGia(giaBan*soLuongMoi);
                spTrongGioHang.setGiaGiam(dbSanPhamCT.getGiaTriGiam());
                spTrongGioHang.setGiaGoc(dbSanPhamCT.getGiaTriSanPham());

                spTrongGioHang.setTrongLuong(dbSanPhamCT.getKhoiLuong());
                spTrongGioHang.setHinhAnh(dbSanPhamCT.getHinhAnh());
                spTrongGioHang.setKichCo(dbSanPhamCT.getKichCo());
                spTrongGioHang.setMauSac(dbSanPhamCT.getMauSac());
            }
        }

        // sản phẩm chưa có trong giỏ : thêm mới vào giỏ
        if (isExisProduct == false) {
            sanPhamTrongGioHang.setSoLuong(soLuongMuonMua);
            sanPhamTrongGioHang.setSanPham(dbSanPhamCT.getSanPham());
            sanPhamTrongGioHang.setMauSac(dbSanPhamCT.getMauSac());
            sanPhamTrongGioHang.setKichCo(dbSanPhamCT.getKichCo());

            sanPhamTrongGioHang.setGiaGoc(dbSanPhamCT.getGiaTriSanPham());
            sanPhamTrongGioHang.setGiaGiam(dbSanPhamCT.getGiaTriGiam());

            Double giaMua;
            Double giaTriGiam = dbSanPhamCT.getGiaTriGiam();
            Double giaTriSanPham = dbSanPhamCT.getGiaTriSanPham();
            System.out.println("Gia tri giam : " + giaTriGiam);
            System.out.println("gia tri san pham : " + giaTriSanPham);

            if (giaTriGiam !=null && giaTriGiam>0) {
                giaMua = giaTriGiam;
            } else {
                giaMua = giaTriSanPham;
            }

            sanPhamTrongGioHang.setGia(giaMua*soLuongMuonMua);
            sanPhamTrongGioHang.setHinhAnh(dbSanPhamCT.getHinhAnh());
            sanPhamTrongGioHang.setTrongLuong(dbSanPhamCT.getKhoiLuong());

            cart.getDs_SanPhamTrongGioHang().add(sanPhamTrongGioHang);

        }


        int tongSoLuongTrongGio = cart.getDs_SanPhamTrongGioHang().size();
        Double tongTien = cart.getDs_SanPhamTrongGioHang().stream().mapToDouble(x -> x.getGia()).sum();

        // lưu cart vào giỏ
        session.setAttribute("cart", cart);
        session.setAttribute("totalCartProducts", tongSoLuongTrongGio);

//        System.out.println("Số lượng sản phẩm thêm vào giỏ : " + sanPhamTrongGioHang.getSoLuongMuaThem());
//        System.out.println("Số lượng sản phẩm trong giỏ : " + cart.getDs_SanPhamTrongGioHang().size());

        Map<String, Object> jsonResult = new HashMap<String, Object>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("totalCartProducts", tongSoLuongTrongGio);
        jsonResult.put("totalPriceResult", tongTien);
        jsonResult.put("soLuongMuaVuotQua", false);
        jsonResult.put("conSoLuongTrongKho", conSoLuongTrongKho);
        jsonResult.put("soLuongMua", soLuongMuonMua);

        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/capNhatSoLuongSPCT")
    public ResponseEntity<Map<String, Object>> updateNumberOfCart(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody SanPhamTrongGioHang sanPhamTrongGioHang
    ) throws IOException {
        HttpSession session = request.getSession();
        GioHang cart = null;

        int soLuongSPTrongGio = sanPhamTrongGioHang.getSoLuong();

        ChiTietSanPham db_chiTietSP = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
        int soLuongSPTrongKho = db_chiTietSP.getSoLuong();
        List<SanPhamTrongGioHang> dsSanPhamGioHangN = new ArrayList<>();

        // nếu giỏ hàng không null, thì lấy session có tên cart gán vào giỏ hàng
        if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
            cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào

        } else {  // chưa có j thì khởi tạo cart mới
            cart = new GioHang();
            session.setAttribute("cart", cart);
        }

        if (soLuongSPTrongGio > soLuongSPTrongKho) {
            System.out.println("== >>>> Số lượng mua vượt quá cho phép ");

            int tongSoLuongTrongGio = dsSanPhamGioHangN.stream().mapToInt(SanPhamTrongGioHang::getSoLuong).sum();
            Double tongTien = cart.getDs_SanPhamTrongGioHang().stream().mapToDouble(x -> x.getGia()).sum();

            // lưu cart vào giỏ
            session.setAttribute("cart", cart);
            session.setAttribute("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
            session.setAttribute("totalPriceResult", tongTien);

            Map<String, Object> jsonResult = new HashMap<String, Object>();
            jsonResult.put("code", 200);
            jsonResult.put("status", "Success");
            jsonResult.put("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
            jsonResult.put("totalPriceResult", tongTien);
            jsonResult.put("soLuongMuaVuotQua", true);

            return ResponseEntity.ok(jsonResult);
        }

        boolean isExisProduct = false;


        // lấy danh sách các sản phẩm trong giỏ hàng thêm vào sản phẩm trong giỏ ( với 1 số thông tin mặc định )
        List<SanPhamTrongGioHang> dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();

//        System.out.Danh sách sản phẩm trong giỏ - khi chưa xóa 2println("Danh sách sản phẩm trong giỏ hàng " + dsSanPhamTrongGio.size());


        Map<String, Object> jsonResult = new HashMap<String, Object>();

        // nếu sản phẩm có trong giỏ hàng rồi thì sửa số lượng
        for (SanPhamTrongGioHang spTrongGioHang : dsSanPhamTrongGio) {
            if (spTrongGioHang.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                isExisProduct = true;
//                System.out.println("Sản phẩm này đã có trong giỏ hàng - đang ở cập nhật số lượng sản phẩm");
                spTrongGioHang.setSoLuong(sanPhamTrongGioHang.getSoLuong());
                Double giaBan = 0.0;
                if(db_chiTietSP.getGiaTriGiam()>0&&db_chiTietSP.getGiaTriGiam()!=null){
                    giaBan = db_chiTietSP.getGiaTriGiam()*sanPhamTrongGioHang.getSoLuong();
                }else {
                    giaBan = db_chiTietSP.getGiaTriSanPham()*sanPhamTrongGioHang.getSoLuong();
                }
                spTrongGioHang.setGia(giaBan);
                break;
            }
        }

        int tongSoLuongTrongGio = dsSanPhamTrongGio.stream().mapToInt(SanPhamTrongGioHang::getSoLuong).sum();
        Double tongTien = cart.getDs_SanPhamTrongGioHang().stream().mapToDouble(x -> x.getGia()).sum();


        // lưu cart vào giỏ
        session.setAttribute("cart", cart);
        session.setAttribute("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
        session.setAttribute("totalPriceResult", tongTien);

        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
        jsonResult.put("totalPriceResult", tongTien);

        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/locTatCaSanPham_online")
    public ResponseEntity<Map<String, Object>> locTatCaSanPham_online(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody LocSanPhamChiTietRequest locSanPhamChiTietRequest
    ) throws IOException {
        List<String> ds_tenMauSac = new ArrayList<>();
        if (locSanPhamChiTietRequest.getDs_uuid_mauSac() != null) {
            ds_tenMauSac = locSanPhamChiTietRequest.getDs_uuid_mauSac();
        } else {
            ds_tenMauSac.add("");
        }

        System.out.println("Danh sách tên mầu sắc search : ");
        for (String mauSac : ds_tenMauSac) {
            System.out.println(mauSac);
        }

        List<String> ds_tenLoaiSP = new ArrayList<>();
        if (locSanPhamChiTietRequest.getDs_uuid_loaiSP() != null) {
            ds_tenLoaiSP = locSanPhamChiTietRequest.getDs_uuid_loaiSP();
        } else {
            ds_tenLoaiSP.add("");
        }

        System.out.println("Danh sách tên loại sản phẩm search : ");
        for (String loaiSP : ds_tenLoaiSP) {
            System.out.println(loaiSP);
        }

        List<String> ds_sizeGiay = new ArrayList<>();
        if (locSanPhamChiTietRequest.getDs_uuid_sizeGiay() != null) {
            ds_sizeGiay = locSanPhamChiTietRequest.getDs_uuid_sizeGiay();
        } else {
            ds_sizeGiay.add("");
        }

        System.out.println("Danh sách tên size giày search : ");
        for (String sizeGiay : ds_sizeGiay) {
            System.out.println(sizeGiay);
        }

//        List<ChiTietSanPham> dsSanPhamCTTimThay =
        List<ChiTietSanPham> dsSanPhamCT = chiTietSPService.locSanPhamChiTiet_layDanhSachSPCT(ds_tenLoaiSP, ds_sizeGiay, ds_tenMauSac);

        System.out.println("Danh sách sản phẩm sau khi tìm kiếm được :");
        for (ChiTietSanPham chiTietSanPham : dsSanPhamCT) {
            System.out.println(chiTietSanPham.toString());
        }

        System.out.println("a><>>>>>>>>>>>>>>>>>>>>");

        // lấy ra danh sách uuid màu sắc , danh sách uuid loại giày
        // truyền vào câu truy vấn lấy ra danh sách kết quả search
        // từ kết quả search = danh sách sản phẩm trong trang này - copy phần xử lý hiển thị danh sách sản phẩm
        // đến lúc fetch ra giao diện : thử xem fetch ra file tương ứng được ko => may be mất hết dữ liệu tìm kiếm
        // => phải dùng fetch data để set dữ liệu sản phẩm tương ứng rồi
        // sửa ngắn gọn thông tin của sản phẩm => tối ưu nhất phần tạo ra sản phẩm được hiển thị được trả về từ server
// lấy danh sách chi tiết sản phẩm

        // lấy danh sách UUID sản phẩm
        List<UUID> ds_uuidSanPham_tuSPCT = new ArrayList<>();
        ds_uuidSanPham_tuSPCT = chiTietSPService.layDanhSach_IdSanPham_trongSanPhamCT();

        // tạo 1 danh sách sản phẩm mới => lưu danh sách sản phẩm chi tiết sẽ hiển thị lên trang chủ
        List<ChiTietSanPham> dsSanPhamCT_new = new ArrayList<>();
        for (UUID idSanPham : ds_uuidSanPham_tuSPCT) {
            for (ChiTietSanPham sanPhamCT : dsSanPhamCT) {
                if (idSanPham.equals(sanPhamCT.getSanPham().getId())) {
                    dsSanPhamCT_new.add(sanPhamCT);
                    break;
                }
            }
        }
//        List<LoaiSanPham> dsLoaiSPTrongSanPhamCT = new ArrayList<>();
//        dsLoaiSPTrongSanPhamCT = chiTietSPService.layTatCa_idLoaiSP_distinct_trongChiTietSP();

//        System.out.println("Size of dsSanPham : " + dsSanPhamCT.size());
        model.addAttribute("dsSanPham", dsSanPhamCT_new);
//        model.addAttribute("dsLoaiSP_trongSanPhamCT", dsLoaiSPTrongSanPhamCT);

        session = request.getSession();
        Integer soLuongSanPhamTrongGio = (Integer) session.getAttribute("totalCartProducts") == null ? 0 : (Integer) session.getAttribute("totalCartProducts");
//        System.out.println("Số lượng sản phẩm trong giỏ : " + soLuongSanPhamTrongGio);
        model.addAttribute("totalCartProducts", soLuongSanPhamTrongGio);

        // lưu cart vào giỏ
//        session.setAttribute("cart", cart);
        session.setAttribute("totalAllProducts", soLuongSanPhamTrongGio);

        Map<String, Object> jsonResult = new HashMap<String, Object>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("totalAllProducts", soLuongSanPhamTrongGio);
        jsonResult.put("danhSachSanPhamCT", dsSanPhamCT_new);


        return ResponseEntity.ok(jsonResult);
    }


    @PostMapping("/layTongSLSanPhamTrongGio")
    public ResponseEntity<Map<String, Object>> layTongSLSanPhamTrongGio(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
    ) throws IOException {
        HttpSession session = request.getSession();
        GioHang cart = null;
        List<SanPhamTrongGioHang> dsSanPhamGioHangN = new ArrayList<>();

        // nếu giỏ hàng không null, thì lấy session có tên cart gán vào giỏ hàng
        if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
            cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào
            dsSanPhamGioHangN = cart.getDs_SanPhamTrongGioHang();

        } else {  // chưa có j thì khởi tạo cart mới
            cart = new GioHang();
            session.setAttribute("cart", cart);
        }

        int tongSoLuongTrongGio = dsSanPhamGioHangN != null ? dsSanPhamGioHangN.stream().mapToInt(SanPhamTrongGioHang::getSoLuong).sum() : 0;
        System.out.println("Tổng số lượng toàn bộ sản phẩm trong giỏ : " + tongSoLuongTrongGio);

        // lưu cart vào giỏ
        session.setAttribute("cart", cart);
        session.setAttribute("totalAllProducts", tongSoLuongTrongGio);

        Map<String, Object> jsonResult = new HashMap<String, Object>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("totalAllProducts", tongSoLuongTrongGio);


        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/xoa/{id}")
    public String xoa(
            @PathVariable("id") UUID id,
            final Model model,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        // lấy ra cart
        HttpSession session = request.getSession();  // tạo mới 1 session
        GioHang cart = null; // khởi tạo 1 Object cart = null
        cart = (GioHang) session.getAttribute("cart");

        // xóa sản phẩm khỏi cart

        List<SanPhamTrongGioHang> dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();

        // xóa sản phẩm khỏi giỏ
        for (int i = 0; i < dsSanPhamTrongGio.size(); i++) {
            if (dsSanPhamTrongGio.get(i).getIdSanPhamCT().equals(id)) {
                dsSanPhamTrongGio.remove(i);
                break;
            }
        }

        cart = new GioHang();
        for (SanPhamTrongGioHang sanPhamTrongGioHang : dsSanPhamTrongGio) {
            cart.getDs_SanPhamTrongGioHang().add(sanPhamTrongGioHang);
        }

        Double tongTien = cart.getDs_SanPhamTrongGioHang().stream().mapToDouble(x -> x.getGia() * x.getSoLuong()).sum();


        // lưu cart vào giỏ
//        session.setAttribute("cart", cart);
        session.setAttribute("cart", cart);
        session.setAttribute("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
        session.setAttribute("totalPriceResult", tongTien);

        // lấy ra tổng tiền được chọn thanh toán
        Double tongTienThanhToan = (Double) session.getAttribute("totalPriceCartThanhToan");

        model.addAttribute("totalPriceCartThanhToan", tongTienThanhToan == null ? 0 : tongTienThanhToan);

//        System.out.println("Xóa khỏi giỏ");
        return "redirect:/gio-hang/view-gio";
    }

    @PostMapping("/add-to-cartThanhToan")
    public ResponseEntity<Map<String, Object>> addToCartThanhToan(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody SanPhamTrongGioHang sanPhamTrongGioHang
    ) throws IOException {
        HttpSession session = request.getSession();
        GioHang cartThanhToan = null; // khởi tạo 1 Object cart = null

        // nếu giỏ hàng không null, thì lấy session có tên cart gán vào giỏ hàng
        if (session.getAttribute("cartThanhToan") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
            cartThanhToan = (GioHang) session.getAttribute("cartThanhToan"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào
        } else {  // chưa có j thì khởi tạo cart mới
            cartThanhToan = new GioHang();
            session.setAttribute("cartThanhToan", cartThanhToan);

//            System.out.println("giỏ hàng chưa có gì cả ");
        }

        Double tongTien = 0.0;

        // trường hợp sản phẩm được chọn numbercheck = 1
        List<SanPhamTrongGioHang> dsSanPhamTrongGioThanhToan = new ArrayList<>();
        if (sanPhamTrongGioHang.getNumberCheck() == 1) {
            if (cartThanhToan.getDs_SanPhamTrongGioHang().size() > 0) {
//                System.out.println("Gio hang thanh toan da co san pham truoc do : " + cartThanhToan.getDs_SanPhamTrongGioHang().size());
                dsSanPhamTrongGioThanhToan = cartThanhToan.getDs_SanPhamTrongGioHang();
            }
//            System.out.println("Danh sách giỏ hàng trước khi thêm " + dsSanPhamTrongGioThanhToan.size());
            SanPhamTrongGioHang sanPhamMoiThem = new SanPhamTrongGioHang();
            ChiTietSanPham chiTietSanPham = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
            sanPhamMoiThem.setIdSanPhamCT(sanPhamTrongGioHang.getIdSanPhamCT());
            sanPhamMoiThem.setSanPham(chiTietSanPham.getSanPham());
            sanPhamMoiThem.setMauSac(chiTietSanPham.getMauSac());
            sanPhamMoiThem.setKichCo(chiTietSanPham.getKichCo());
            sanPhamMoiThem.setGia(((chiTietSanPham.getGiaTriGiam() > 0) ? chiTietSanPham.getGiaTriGiam() : chiTietSanPham.getGiaTriSanPham())*sanPhamTrongGioHang.getSoLuong());

            sanPhamMoiThem.setGiaGiam(chiTietSanPham.getGiaTriGiam());
            sanPhamMoiThem.setGiaGoc(chiTietSanPham.getGiaTriSanPham());

            sanPhamMoiThem.setNumberCheck(1);
            sanPhamMoiThem.setSoLuong(sanPhamTrongGioHang.getSoLuong());
            sanPhamMoiThem.setHinhAnh(chiTietSanPham.getHinhAnh());  // chưa xử lý được hình ảnh
            sanPhamMoiThem.setTrongLuong(chiTietSanPham.getKhoiLuong());


            // thêm sản phẩm vào giỏ trong list
            dsSanPhamTrongGioThanhToan.add(sanPhamMoiThem);

            // add lại toàn bộ dữ liệu thêm vào cart thanh toán
            cartThanhToan = new GioHang();
            for (SanPhamTrongGioHang sanPhamGio : dsSanPhamTrongGioThanhToan) {
                cartThanhToan.getDs_SanPhamTrongGioHang().add(sanPhamGio);
            }

            // tính tổng tiền giỏ hàng thanh toán
            tongTien = dsSanPhamTrongGioThanhToan.stream()
                    .mapToDouble(sanPham ->  sanPham.getGia())
                    .sum();

            session.setAttribute("cartThanhToan", cartThanhToan);
            model.addAttribute("totalPriceCartThanhToan", tongTien);

        } else { // không được chọn
            if (sanPhamTrongGioHang.getNumberCheck() == 1) {
                cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
                dsSanPhamTrongGioThanhToan = cartThanhToan.getDs_SanPhamTrongGioHang();

                // kiểm tra id sản phẩm có trong giỏ hàng thanh toán chưa ? (có )

                List<SanPhamTrongGioHang> ds_addProductNewToCart = new ArrayList<>();
                for (SanPhamTrongGioHang spCartThanhToan : dsSanPhamTrongGioThanhToan) {
                    if (spCartThanhToan.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {

                        spCartThanhToan.setSoLuong(sanPhamTrongGioHang.getSoLuong());

                        // lấy thông tin sản phẩm trong DB => set dữ liệu cho sản phẩm trong giỏ
                        ChiTietSanPham db_chiTietSP = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
                        spCartThanhToan.setTrongLuong(db_chiTietSP.getKhoiLuong());
                        spCartThanhToan.setGiaGoc(db_chiTietSP.getGiaTriSanPham());
                        spCartThanhToan.setGiaGiam(db_chiTietSP.getGiaTriGiam());
                        Double donGia = 0.0;
                        if(db_chiTietSP.getGiaTriGiam()>0 && db_chiTietSP.getGiaTriGiam()!=null){
                            donGia = db_chiTietSP.getGiaTriGiam()*sanPhamTrongGioHang.getSoLuong();
                        }else {
                            donGia = db_chiTietSP.getGiaTriSanPham()*sanPhamTrongGioHang.getSoLuong();
                        }
                        spCartThanhToan.setGia(donGia);
                        spCartThanhToan.setMauSac(db_chiTietSP.getMauSac());
                        spCartThanhToan.setHinhAnh(db_chiTietSP.getHinhAnh());
                        spCartThanhToan.setKichCo(db_chiTietSP.getKichCo());
                        spCartThanhToan.setIdSanPhamCT(db_chiTietSP.getId());

                    } else {
                        SanPhamTrongGioHang sanPhamMoiThem = new SanPhamTrongGioHang();
                        ChiTietSanPham chiTietSanPham = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());

                        sanPhamMoiThem.setIdSanPhamCT(sanPhamTrongGioHang.getIdSanPhamCT());
                        sanPhamMoiThem.setSanPham(chiTietSanPham.getSanPham());
                        sanPhamMoiThem.setMauSac(chiTietSanPham.getMauSac());
                        sanPhamMoiThem.setKichCo(chiTietSanPham.getKichCo());

                        Double donGia = 0.0;
                        if(chiTietSanPham.getGiaTriGiam()>0 && chiTietSanPham.getGiaTriGiam()!=null){
                            donGia = chiTietSanPham.getGiaTriGiam()*sanPhamTrongGioHang.getSoLuong();
                        }else {
                            donGia = chiTietSanPham.getGiaTriSanPham()*sanPhamTrongGioHang.getSoLuong();
                        }

                        sanPhamMoiThem.setGia(donGia);

                        sanPhamMoiThem.setGiaGoc(chiTietSanPham.getGiaTriSanPham());
                        sanPhamMoiThem.setGiaGiam(chiTietSanPham.getGiaTriGiam());

                        sanPhamMoiThem.setNumberCheck(1);
                        sanPhamMoiThem.setSoLuong(sanPhamTrongGioHang.getSoLuong());
                        sanPhamMoiThem.setHinhAnh(chiTietSanPham.getHinhAnh());  // chưa xử lý được hình ảnh
                        sanPhamMoiThem.setTrongLuong(chiTietSanPham.getKhoiLuong());  // chưa xử lý được hình ảnh

                        // thêm sản phẩm vào giỏ trong list
                        ds_addProductNewToCart.add(sanPhamMoiThem);
                   }
                }

                dsSanPhamTrongGioThanhToan.addAll(ds_addProductNewToCart);
                tongTien = dsSanPhamTrongGioThanhToan.stream()
                        .mapToDouble(sanPham ->  sanPham.getGia()) // Ánh xạ sản phẩm thành giá trị tiền
                        .sum();

                // thêm các sản phẩm lại vào cartThanhToan
                cartThanhToan = new GioHang(); // khởi tạo lại , thêm lại các sản phẩm vào giỏ hàng thanh toán
                for (SanPhamTrongGioHang sanPham : dsSanPhamTrongGioThanhToan) {
                    cartThanhToan.getDs_SanPhamTrongGioHang().add(sanPham);
                }

                // thêm danh sách sản phẩm vào session
                session.setAttribute("cartThanhToan", cartThanhToan);
//                model.addAttribute("totalPriceCartThanhToan", tongTien);

            } else {
//                System.out.println("Sản phẩm được bỏ chọn khỏi giỏ hàng" + sanPhamTrongGioHang.getNumberCheck());
                // kiểm tra id sản phẩm có trong giỏ hàng thanh toán chưa ? (có )
                // => xóa sản phẩm khỏi cart
                int checkXoa = 0;
                SanPhamTrongGioHang xoaSanPhamGio = new SanPhamTrongGioHang();
                cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
                dsSanPhamTrongGioThanhToan = cartThanhToan.getDs_SanPhamTrongGioHang();
//                System.out.println("Danh sách sản phẩm trong giỏ - sau khi vào hàm check bỏ chọn" + dsSanPhamTrongGioThanhToan.size());
                for (int i = 0; i < dsSanPhamTrongGioThanhToan.size(); i++) {
                    if (dsSanPhamTrongGioThanhToan.get(i).getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                        xoaSanPhamGio = dsSanPhamTrongGioThanhToan.get(i);
                        break;
                    }
                }
                dsSanPhamTrongGioThanhToan.remove(xoaSanPhamGio);

                tongTien = dsSanPhamTrongGioThanhToan.stream()
                        .mapToDouble(sanPham -> sanPham.getGia())
                        .sum();


                cartThanhToan = new GioHang(); // khởi tạo lại , thêm lại các sản phẩm vào giỏ hàng thanh toán
                for (SanPhamTrongGioHang sanPham : dsSanPhamTrongGioThanhToan) {
                    cartThanhToan.getDs_SanPhamTrongGioHang().add(sanPham);
                }

                // thêm danh sách sản phẩm vào session
                session.setAttribute("cartThanhToan", cartThanhToan);
//                model.addAttribute("totalPriceCartThanhToan", tongTien);
            }
        }

        // lưu dữ liệu vào json
        Map<String, Object> jsonResult = new HashMap<String, Object>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("totalPriceCartThanhToan", tongTien);

        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/mua-ngay")
    public ResponseEntity<Map<String, Object>> muaNgay(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody SanPhamTrongGioHang sanPhamTrongGioHang
    ) throws IOException {
        chiTietSPService.capNhatGiaTriGiamNull();

        int soLuongMuonMua = sanPhamTrongGioHang.getSoLuong();
        int soLuongSPTrongGio = 0;
        boolean conSoLuongTrongKho = true;

        ChiTietSanPham chiTietSanPham = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
        int soLuongSPTrongKho = chiTietSanPham.getSoLuong();
        if(soLuongSPTrongKho<=0){
            conSoLuongTrongKho = false;
        }

        HttpSession session = request.getSession();  // tạo mới 1 session
        GioHang cart = null; // khởi tạo 1 Object cart = null

        // nếu giỏ hàng không null, thì lấy session có tên cart gán vào giỏ hàng
        List<SanPhamTrongGioHang> dsSanPhamGioHangN = new ArrayList<>();
        if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
            cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào

            dsSanPhamGioHangN = cart.getDs_SanPhamTrongGioHang();
            for (SanPhamTrongGioHang sanPhamDuocChon : dsSanPhamGioHangN) {
                if (sanPhamDuocChon.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                    soLuongSPTrongGio = sanPhamDuocChon.getSoLuong();
                    break;
                }
            }
        } else {  // chưa có j thì khởi tạo cart mới
            cart = new GioHang();
            session.setAttribute("cart", cart);
//            System.out.println("giỏ hàng chưa có gì cả ");
        }

        if ((soLuongMuonMua + soLuongSPTrongGio) > soLuongSPTrongKho) {
//            model.addAttribute("soLuongMuaVuotQua",true);
            Map<String, Object> jsonResult = new HashMap<String, Object>();
            jsonResult.put("code", 200);
            jsonResult.put("status", "Success");
            int tongSoLuongTrongGio = (dsSanPhamGioHangN != null ? dsSanPhamGioHangN.size() : 0);
            jsonResult.put("totalCartProducts", tongSoLuongTrongGio);
            jsonResult.put("soLuongMuaVuotQua", true);
            jsonResult.put("giaTriSoLuongCoTheMua", (soLuongSPTrongKho - soLuongSPTrongGio) > 0 ? (soLuongSPTrongKho - soLuongSPTrongGio) : 0);

            jsonResult.put("soLuongSPTrongKho",soLuongSPTrongKho);
            jsonResult.put("soLuongMua",soLuongMuonMua);
            jsonResult.put("conSoLuongTrongKho",conSoLuongTrongKho);


            return ResponseEntity.ok(jsonResult);
        }

        boolean isExisProduct = false;

        ChiTietSanPham dbSanPhamCT = new ChiTietSanPham();
        if(sanPhamTrongGioHang.getIdSanPhamCT()!=null){
            dbSanPhamCT = chiTietSPService.chiTietTheoId(sanPhamTrongGioHang.getIdSanPhamCT());
        }


        // lấy danh sách các sản phẩm trong giỏ hàng thêm vào sản phẩm trong giỏ ( với 1 số thông tin mặc định )
        List<SanPhamTrongGioHang> dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();

        // nếu sản phẩm có trong giỏ hàng rồi thì sửa số lượng
        for (SanPhamTrongGioHang spTrongGioHang : dsSanPhamTrongGio) {

            if (spTrongGioHang.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                isExisProduct = true;
//                System.out.println("Sản phẩm này đã có trong giỏ hàng");
//                spTrongGioHang.setSoLuong(spTrongGioHang.getSoLuong() + 1);

                Integer soLuongMoi = spTrongGioHang.getSoLuong() + sanPhamTrongGioHang.getSoLuongMuaThem();
                spTrongGioHang.setSoLuong(soLuongMoi);
                // cập nhật số lượng => cập nhật thành tiền => tổng tiền thanh toán = tổng tất cả thành tiền
                // muốn tính được giá => lấy được giá bán sản phẩm
                // giá bán sản phẩm lại có 2 trường hợp
                // lấy giá bán sản phẩm tại đâu => trong DB
                Double giaBan = 0.0;
                if(dbSanPhamCT.getGiaTriGiam()>0 && dbSanPhamCT.getGiaTriGiam()!=null){
                    giaBan = dbSanPhamCT.getGiaTriGiam();
                }else {
                    giaBan = dbSanPhamCT.getGiaTriSanPham();
                }
                spTrongGioHang.setGia(giaBan*soLuongMoi);
                spTrongGioHang.setGiaGiam(dbSanPhamCT.getGiaTriGiam());
                spTrongGioHang.setGiaGoc(dbSanPhamCT.getGiaTriSanPham());

                spTrongGioHang.setTrongLuong(dbSanPhamCT.getKhoiLuong());
                spTrongGioHang.setHinhAnh(dbSanPhamCT.getHinhAnh());
                spTrongGioHang.setKichCo(dbSanPhamCT.getKichCo());
                spTrongGioHang.setMauSac(dbSanPhamCT.getMauSac());
            }
        }


        // cart ở đây chính là danh sách sản ơphaarm luôn
        // ! ngược lại true = false nghĩa là isExis ko thay đổi => sản phẩm chưa có trong giỏ
        if (isExisProduct == false) {
            sanPhamTrongGioHang.setSanPham(dbSanPhamCT.getSanPham());
            sanPhamTrongGioHang.setMauSac(dbSanPhamCT.getMauSac());
            sanPhamTrongGioHang.setKichCo(dbSanPhamCT.getKichCo());
            sanPhamTrongGioHang.setGiaGoc(dbSanPhamCT.getGiaTriSanPham());
            sanPhamTrongGioHang.setGiaGiam(dbSanPhamCT.getGiaTriGiam());

            Double giaMua;
            Double giaTriGiam = dbSanPhamCT.getGiaTriGiam();
            Double giaTriSanPham = dbSanPhamCT.getGiaTriSanPham();
//            System.out.println("Gia tri giam : " + giaTriGiam);
//            System.out.println("gia tri san pham : " + giaTriSanPham);

            if (giaTriGiam !=null && giaTriGiam>0) {
                giaMua = giaTriGiam;
            } else {
                giaMua = giaTriSanPham;
            }

//            System.out.println("Gia mua : " + giaMua);
            sanPhamTrongGioHang.setGia(giaMua*soLuongMuonMua);
            sanPhamTrongGioHang.setHinhAnh(dbSanPhamCT.getHinhAnh());
            sanPhamTrongGioHang.setTrongLuong(dbSanPhamCT.getKhoiLuong());

            cart.getDs_SanPhamTrongGioHang().add(sanPhamTrongGioHang);
        }

        List<SanPhamTrongGioHang> dsSanPhamTrongGios = new ArrayList<>();
        dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();
        model.addAttribute("sanPhamTrongGio", dsSanPhamTrongGios);

        Double tongTien = 0.0;
        if (cart.getDs_SanPhamTrongGioHang().size() > 0) {
            for (SanPhamTrongGioHang dsSP : cart.getDs_SanPhamTrongGioHang()) {
                tongTien += dsSP.getGia();
            }
        }

        // lưu cart vào giỏ
        session.setAttribute("cart", cart);
        session.setAttribute("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
        session.setAttribute("totalPriceResult", tongTien);

        Map<String, Object> jsonResult = new HashMap<String, Object>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("totalCartProducts", cart.getDs_SanPhamTrongGioHang().size());
        jsonResult.put("totalPriceResult", tongTien);
        jsonResult.put("soLuongMuaVuotQua", false);

        jsonResult.put("soLuongSPTrongKho",soLuongSPTrongKho);
        jsonResult.put("soLuongMua",soLuongMuonMua);
        jsonResult.put("conSoLuongTrongKho",conSoLuongTrongKho);

        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/delete-product-in-cart")
    public ResponseEntity<Map<String, Object>> xoaSanPhamKhoiCartThanhToan(
            Model model,
            HttpServletRequest httpServletRequest
    ) {
        HttpSession session = httpServletRequest.getSession();  // tạo mới 1 session
        GioHang cartThanhToan = new GioHang();
        session.setAttribute("cartThanhToan", cartThanhToan);

        Map<String, Object> jsonResult = new HashMap<String, Object>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/ketQuaThanhToanVNPay")
    public String ketQuaThanhToan(
            HttpServletRequest request
    ) throws ParseException {
        HttpSession session = request.getSession();  // tạo mới 1 session
        HoaDon hoaDonVuaThanhToan = new HoaDon();

        String vnp_TxnRef = (String) session.getAttribute("vnp_TxnRef");
        System.out.println("vnp_txnRef" + vnp_TxnRef);

        if(session.getAttribute("hoaDonVuaDatOnline")!=null){
            hoaDonVuaThanhToan = (HoaDon) session.getAttribute("hoaDonVuaDatOnline");
        }else {
            session.setAttribute("hoaDonVuaDatOnline",new HoaDon());
        }

//        System.out.println("Chạy vào url ket qua thanh toan");
        // lấy ra danh sách giỏ hàng thanh toán
        GioHang gioHangThanhToan = (GioHang) session.getAttribute("cartThanhToan");

        HoaDonRequest datHangRequest = (HoaDonRequest) session.getAttribute("ThongTinDatHang");

        HoaDon hoaDon = new HoaDon();
        hoaDon = hoaDonService.chiTietTheoId(hoaDonVuaThanhToan.getId());

        // tạo mã hóa đơn random
//        int randomInt = (int) (Math.random() * 100);
//        String maHoaDon = String.valueOf(randomInt);
//        hoaDon.setTenKH(datHangRequest.getHoVaTen());
//        hoaDon.setDiaChi(datHangRequest.getDiaChi());
//        hoaDon.setGhiChu(datHangRequest.getGhiChu());
//        hoaDon.setSoDT(datHangRequest.getSoDT());
////        hoaDon.setTongTienThanhToan(gioHangThanhToan.tongTienTrongGioHang()*1000);
//        hoaDon.setEmail(datHangRequest.getEmail());
//        hoaDon.setNgayTao(Constant.getDateNowByTime());
//        hoaDon.setNgaySua(Constant.getDateNowByTime());
//        hoaDon.setNgayMua(Constant.getDateNow());
//        hoaDon.setNgayThanhToan(Constant.getDateNow());
//        hoaDon.setTinh_thanhPho(datHangRequest.getTinh_thanhPho());
//        hoaDon.setQuan_huyen(datHangRequest.getQuan_huyen());
//        hoaDon.setPhuong_xa(datHangRequest.getPhuong_xa());
//        hoaDon.setHinhThucTT("2");
//        hoaDon.setLoaiHoaDon("2");
//
//        hoaDon.setTienVanChuyen(datHangRequest.getPhiShip()*1000);

        GioHang cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
//        List<SanPhamTrongGioHang> dsSanPhamTrongGio = new ArrayList<>();
//        if (cartThanhToan != null) {
//            dsSanPhamTrongGio = cartThanhToan.getDs_SanPhamTrongGioHang();
//        }
//        Double tongTienDonHang = dsSanPhamTrongGio.stream().mapToDouble(sp -> sp.getGia()).sum();
//
//        System.out.println("Tổng tiền thanh toán : " + (tongTienDonHang + datHangRequest.getPhiShip() * 1000));
//        Double tongTienThanhToan = tongTienDonHang  + datHangRequest.getPhiShip() * 1000;
//
//        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDon.setTrangThai("2");
        hoaDon.setNgayThanhToan(Constant.getDateNow());
//        hoaDon.setMa("HD-" + maHoaDon);

        // thêm thông tin đơn hàng vào hóa đơn - trong database
        HoaDon hoaDon1 = hoaDonService.capNhat(hoaDon);
//        UUID idHoaDonVuaTao = hoaDon1.getId();
//        String idHoaDonString = idHoaDonVuaTao.toString();
//        String kyTuCuoi_id = idHoaDonString.substring(idHoaDonString.length() - 4);
//        String timeSystemStr = Constant.getDate_TimeHeThongCurrent();
//        String maHoaDonNew = "HD-"+timeSystemStr+kyTuCuoi_id;
//
//        hoaDon1.setMa(maHoaDonNew);

        emailSenderService.sendSimpleEmail(hoaDon1.getEmail(),"Đơn hàng thanh toán thành công bằng VNPay tại NiceSport",
                "Bạn có thể dùng mã hóa đơn để tra cứu tình trạng đơn hàng: " +hoaDon1.getMa()
                +"\nClick vào link để tra cứu đơn hàng vừa đặt:"+"http://localhost:8080/gio-hang/view-traThongTinDonHang?searchByMaHoaDon="+hoaDon1.getMa()
                +"\nClick tại đây để tiếp tục mua hàng: " + "http://localhost:8080/gio-hang/view-trangChu2"
                + "\n\n Cảm ơn bạn đã tin tưởng và đồng hành cùng Nice Sport!");

//        session.setAttribute("hoaDonVuaDatOnline", hoaDon1);

        UUID id_hoaDon = hoaDon1.getId();

//        System.out.println("Thêm mới hóa đơn thành công ");

        // lấy hóa đơn ra theo mã hóa đơn phía trên => thêm sản phẩm vào hóa đơn chi tiết
//        HoaDon hoaDon1 = hoaDonService.layHoaDonTheoMa(maHoaDon);

        // lấy ra danh sách sản phẩm được thêm vào cart thanh toán
        List<SanPhamTrongGioHang> dsSanPhamTrongGioThanhToan = gioHangThanhToan.getDs_SanPhamTrongGioHang();


        // duyệt list sau đó thêm sản phẩm chi tiết vào hóa đơn chi tiết
        for (SanPhamTrongGioHang sanPhamGio : dsSanPhamTrongGioThanhToan) {
//            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
//            // chi tiet san pham
//            ChiTietSanPham chiTietSanPham = new ChiTietSanPham();
//            chiTietSanPham = chiTietSPService.chiTietTheoId(sanPhamGio.getIdSanPhamCT());
//
//            hoaDonChiTiet.setChiTietSanPham(chiTietSanPham);
//            // hóa đơn
//            HoaDon hoaDon2 = new HoaDon();
//            hoaDon2.setId(id_hoaDon);
//            hoaDonChiTiet.setHoaDon(hoaDon2);
//
//            Double giaGiam = 0.0;
//            if(chiTietSanPham.getGiaTriGiam()!=null){
//                giaGiam = chiTietSanPham.getGiaTriGiam()<=0||chiTietSanPham.getGiaTriGiam()==null?0:chiTietSanPham.getGiaTriGiam();
//
//            }
//
//            hoaDonChiTiet.setGiaSauGiam(giaGiam);
//            hoaDonChiTiet.setGiaGocSP(chiTietSanPham.getGiaTriSanPham());
//            hoaDonChiTiet.setDonGia(sanPhamGio.getGia());
//            hoaDonChiTiet.setSoLuong(sanPhamGio.getSoLuong());
//            hoaDonChiTiet.setNgayTao(Constant.getDateNow());
//            hoaDonChiTiet.setTrangThai(1);
//
//            // them sản phẩm vào hóa đơn chi tiết
//            hoaDonChiTietService.themMoi(hoaDonChiTiet);
            // cập nhật số lượng sản phẩm trong DB
            chiTietSPService.capNhatSoLuongSauKhiDatHang(sanPhamGio.getSoLuong(), sanPhamGio.getIdSanPhamCT());
        }

        // duyệt danh sách sản phẩm trong giỏ hàng => kiểm tra cái nào có được chọn mua => xóa
//        HttpSession session = request.getSession();

        // lấy ra danh sách giỏ hàng thanh toán
        GioHang gioHangCuaKhach = (GioHang) session.getAttribute("cart");
        List<SanPhamTrongGioHang> dsSanPhamGioKH = gioHangCuaKhach.getDs_SanPhamTrongGioHang();

// Danh sách tạm để lưu các phần tử cần xóa
        List<SanPhamTrongGioHang> toRemoveFromCart = new ArrayList<>();
        List<SanPhamTrongGioHang> toRemoveFromCartThanhToan = new ArrayList<>();

// Duyệt qua các sản phẩm trong giỏ hàng của khách
        for (SanPhamTrongGioHang element1 : dsSanPhamGioKH) {
            for (SanPhamTrongGioHang element2 : dsSanPhamTrongGioThanhToan) {
                if (element1.getIdSanPhamCT().equals(element2.getIdSanPhamCT())) {
                    toRemoveFromCart.add(element1);
                    toRemoveFromCartThanhToan.add(element2);
                }
            }
        }

// Xóa các phần tử đã đánh dấu khỏi dsSanPhamGioKH và dsSanPhamTrongGioThanhToan
        dsSanPhamGioKH.removeAll(toRemoveFromCart);
        dsSanPhamTrongGioThanhToan.removeAll(toRemoveFromCartThanhToan);


//        System.out.println("Danh sách sản phẩm trong giỏ - sau khi xóa " + dsSanPhamGioKH.size());
//        System.out.println("Danh sách sản phẩm trong giỏ thanh toán - sau khi xóa " + dsSanPhamTrongGioThanhToan.size());

        // xóa hết cart thanh toán => vì đã hoàn thành đặt hàng rồi
        GioHang capNhatGioHangKhachHang = new GioHang();
        if (dsSanPhamGioKH.size() > 0) {
            for (SanPhamTrongGioHang sanPhamGio : dsSanPhamGioKH) {
                capNhatGioHangKhachHang.getDs_SanPhamTrongGioHang().add(sanPhamGio);
            }
        }

        session.setAttribute("cart", capNhatGioHangKhachHang);
        session.setAttribute("cartThanhToan", new GioHang());

        int tongSoLuongTrongGio = dsSanPhamGioKH.stream().mapToInt(SanPhamTrongGioHang::getSoLuong).sum();

        session.setAttribute("totalCartProducts", tongSoLuongTrongGio);

        session.setAttribute("ThongTinDatHang", new HoaDonRequest());

//        System.out.println("Đặt hàng thành công sau khi thanh toán vnpay");
        return "redirect:/gio-hang/view-ketQuaThanhToan";
    }

//    @PostMapping("/capNhatSoLuongSPCT")
//    public ResponseEntity<Map<String, Object>> updateNumberOfCart(

    @GetMapping("/kiemTraSoLuongSPTrongGio")
    public ResponseEntity<Map<String, Object>> kiemTraSoLuongSPTrongGio(
            Model model,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession();
        GioHang cart = (GioHang) session.getAttribute("cart");

        boolean checkResult = true;
        if (cart == null) {
            checkResult = false;
        }

        Map<String, Object> jsonResult = new HashMap<>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("checkResult", checkResult);

        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/kiemTraLai_soLuongTonKho")
    public ResponseEntity<Map<String, Object>> kiemTraLai_soLuongTonKho(
            Model model,
            HttpServletRequest request
    ) {
        boolean coSanPham_khongconHangTrongKho = false;

        HttpSession session = request.getSession();
        GioHang cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
        GioHang cart = (GioHang) session.getAttribute("cart");
        // lấy ra danh sách cartthanh toán
        List<SanPhamTrongGioHang> dsSanPham_cartThanhToan = new ArrayList<>();
        dsSanPham_cartThanhToan = cartThanhToan.getDs_SanPhamTrongGioHang();


        List<SanPhamTrongGioHang> ds_sanPham_hetHangTrongKho = new ArrayList<>();

        // check từng số lượng sản phẩm với trong kho
        for(SanPhamTrongGioHang spChonThanhToan : dsSanPham_cartThanhToan){
            // lấy ra số lượng sản phẩm trong kho
            ChiTietSanPham sanPhamKho = new ChiTietSanPham();
            sanPhamKho = chiTietSPService.chiTietTheoId(spChonThanhToan.getIdSanPhamCT());
            Integer soLuongSPKho = sanPhamKho.getSoLuong();

            if(soLuongSPKho<=0){ // lấy các sản phẩm còn số lượng kho trong view thanh toán > 0
                coSanPham_khongconHangTrongKho = true;
                // thêm id sản phẩm vào danh sách các sản phẩm hết hàng trong kho
                ds_sanPham_hetHangTrongKho.add(spChonThanhToan);
            }
        }
        System.out.println("Số lượng sản phẩm ko hợp lệ: " + ds_sanPham_hetHangTrongKho.size());

        // nếu coSanPham_khongconHangTrongKho = true; mới thực hiện các code xử lý bên dưới
        if(coSanPham_khongconHangTrongKho==true){
            // xóa hết sản phẩm trong cart thanh toán
//            GioHang cartThanhToanNew = new GioHang();
//            session.setAttribute("cartThanhToan", cartThanhToanNew);

            // - thêm các sản phẩm còn hàng vào lại giỏ hàng

            // lấy ra danh sách cartthanh toán
            List<SanPhamTrongGioHang> dsSanPham_gioHang = new ArrayList<>();

            if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
                cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào
                dsSanPham_gioHang = cart.getDs_SanPhamTrongGioHang();
            } else {  // chưa có j thì khởi tạo cart mới
                cart = new GioHang();
                session.setAttribute("cart", cart);
            }

            int count_ptu_xoa = 0;
            // duyệt danh sách sản phẩm trong giỏ hàng - xóa đi các sản phẩm hết hàng
            if(ds_sanPham_hetHangTrongKho.size()>0){
                for (int i = 0; i < ds_sanPham_hetHangTrongKho.size(); i++) {
//                    if (dsSanPham_gioHang.size() > 0) {
                        ListIterator<SanPhamTrongGioHang> iterator = dsSanPham_gioHang.listIterator();
                        while (iterator.hasNext()) {
                            SanPhamTrongGioHang sanPhamTrongGioHang = iterator.next();
                            if (ds_sanPham_hetHangTrongKho.get(i).getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                                iterator.remove();
                                count_ptu_xoa++;
                            }
                        }
//                    }
                }
            }
            System.out.println("Số lượng phần tử hết hàng trong kho bị xóa : " + count_ptu_xoa);
            // làm sao để xóa các trường hợp ko hợp lệ ?


            // cập nhật lại giỏ hàng
            cart.setDs_SanPhamTrongGioHang(dsSanPham_gioHang);
            session.setAttribute("cart", cart);
        }

        Map<String, Object> jsonResult = new HashMap<>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("coSanPham_khongconHangTrongKho", coSanPham_khongconHangTrongKho);

        return ResponseEntity.ok(jsonResult);
    }

    @GetMapping("/kiemTraLai_soLuongTonKho_truocKhiBanThanhToan")
    public ResponseEntity<Map<String, Object>> kiemTraLai_soLuongTonKho_truocKhiBanThanhToan(
            Model model,
            HttpServletRequest request
    ) {
        boolean coSanPham_khongconHangTrongKho = true;

        HttpSession session = request.getSession();
//        GioHang cartThanhToan = (GioHang) session.getAttribute("cartThanhToan");
        GioHang cart = (GioHang) session.getAttribute("cart");
        // lấy ra danh sách cartthanh toán
        List<SanPhamTrongGioHang> dsSanPham_cart = new ArrayList<>();
        dsSanPham_cart = cart.getDs_SanPhamTrongGioHang();


        List<SanPhamTrongGioHang> ds_sanPham_hetHangTrongKho = new ArrayList<>();

        // check từng số lượng sản phẩm với trong kho
        for(SanPhamTrongGioHang spChonThanhToan : dsSanPham_cart){
            // lấy ra số lượng sản phẩm trong kho
            ChiTietSanPham sanPhamKho = new ChiTietSanPham();
            sanPhamKho = chiTietSPService.chiTietTheoId(spChonThanhToan.getIdSanPhamCT());
            Integer soLuongSPKho = sanPhamKho.getSoLuong();

            if(soLuongSPKho<=0){ // lấy các sản phẩm còn số lượng kho trong view thanh toán > 0
                coSanPham_khongconHangTrongKho = false;
                // thêm id sản phẩm vào danh sách các sản phẩm hết hàng trong kho
                ds_sanPham_hetHangTrongKho.add(spChonThanhToan);
            }
        }
        System.out.println("Số lượng sản phẩm ko hợp lệ: " + ds_sanPham_hetHangTrongKho.size());

        // nếu coSanPham_khongconHangTrongKho = true; mới thực hiện các code xử lý bên dưới
        if(coSanPham_khongconHangTrongKho==false){
            // xóa hết sản phẩm trong cart
//            GioHang cartThanhToanNew = new GioHang();
//            session.setAttribute("cartThanhToan", cartThanhToanNew);

            // - thêm các sản phẩm còn hàng vào lại giỏ hàng

            // lấy ra danh sách cart
            List<SanPhamTrongGioHang> dsSanPham_gioHang = new ArrayList<>();

            if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
                cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào
                dsSanPham_gioHang = cart.getDs_SanPhamTrongGioHang();
            } else {  // chưa có j thì khởi tạo cart mới
                cart = new GioHang();
                session.setAttribute("cart", cart);
            }

            int count_ptu_xoa = 0;
            // duyệt danh sách sản phẩm trong giỏ hàng - xóa đi các sản phẩm hết hàng
            if(ds_sanPham_hetHangTrongKho.size()>0){
                for (int i = 0; i < ds_sanPham_hetHangTrongKho.size(); i++) {
                    ListIterator<SanPhamTrongGioHang> iterator = dsSanPham_gioHang.listIterator();
                    while (iterator.hasNext()) {
                        SanPhamTrongGioHang sanPhamTrongGioHang = iterator.next();
                        if (ds_sanPham_hetHangTrongKho.get(i).getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())) {
                            iterator.remove();
                            count_ptu_xoa++;
                        }
                    }
                }
            }
            System.out.println("Số lượng phần tử hết hàng trong kho bị xóa : " + count_ptu_xoa);
            // làm sao để xóa các trường hợp ko hợp lệ ?


            // cập nhật lại giỏ hàng
            cart.setDs_SanPhamTrongGioHang(dsSanPham_gioHang);
            session.setAttribute("cart", cart);

            // xóa hết sản phẩm được thêm vào cart thanh toán
            session.setAttribute("cartThanhToan",new GioHang());
        }

        Map<String, Object> jsonResult = new HashMap<>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("coSanPham_khongconHangTrongKho", coSanPham_khongconHangTrongKho);
        jsonResult.put("tongSLMatHangTrongGio", cart.getDs_SanPhamTrongGioHang().size());
        System.out.println("Tổng sl mặt hàng trong giỏ : " + cart.getDs_SanPhamTrongGioHang().size());

        System.out.println("coSanPham_khongconHangTrongKho /kiemTraLai_soLuongTonKho_truocKhiBanThanhToan : " + coSanPham_khongconHangTrongKho);

        return ResponseEntity.ok(jsonResult);
    }

    @PostMapping("/layTongSLTrongGio")
    public ResponseEntity<Map<String, Object>> layTongSLTrongGio(
            final Model model
            , final HttpServletRequest request
            , final HttpServletResponse response
            , @RequestBody SanPhamTrongGioHang sanPhamTrongGioHang
    ) throws IOException {
        HttpSession session = request.getSession();
        GioHang cart = null;

        List<SanPhamTrongGioHang> dsSanPhamTrongGio = new ArrayList<>();

        // nếu giỏ hàng không null, thì lấy session có tên cart gán vào giỏ hàng
        if (session.getAttribute("cart") != null) {
//            System.out.println("có sản phẩm trong giỏ hàng");
            cart = (GioHang) session.getAttribute("cart"); // nếu cart đang tồn tại giá trị thì gán giá trị đang tồn tại của cart này vào
            dsSanPhamTrongGio = cart.getDs_SanPhamTrongGioHang();
        } else {  // chưa có j thì khởi tạo cart mới
            cart = new GioHang();
            session.setAttribute("cart", cart);
        }

        Integer tongSoLuongSP = 0;
        if (dsSanPhamTrongGio != null) {
            System.out.println("Size danh sách sản phẩm trong giỏ : " + dsSanPhamTrongGio.size());
            for (SanPhamTrongGioHang sanPhamTrongGio : dsSanPhamTrongGio) {
                if(!sanPhamTrongGio.getIdSanPhamCT().equals(sanPhamTrongGioHang.getIdSanPhamCT())){
                    tongSoLuongSP += sanPhamTrongGio.getSoLuong();
                }
            }
        }

        System.out.println("Tổng số lượng sản phẩm còn lại khác : " + tongSoLuongSP);

        Map<String, Object> jsonResult = new HashMap<>();
        jsonResult.put("code", 200);
        jsonResult.put("status", "Success");
        jsonResult.put("tongSLTatCaSP", tongSoLuongSP);

//        System.out.println("Chạy vào giỏ hàng xong ... cần redirect lại giỏ hàng ");
        return ResponseEntity.ok(jsonResult);
    }
}
    