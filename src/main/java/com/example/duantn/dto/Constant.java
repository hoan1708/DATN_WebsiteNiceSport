package com.example.duantn.dto;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class Constant {
    public static Integer pageNumber = 7;

    public static Date getDateNow() throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = sdf.format(cal.getTime());
        java.util.Date ngayTao = sdf.parse(strDate);
        java.sql.Date ngayTaoSQL = new java.sql.Date(ngayTao.getTime());
        return ngayTaoSQL;
    }

    public static java.util.Date getDateNowByTime() throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String strDate = sdf.format(cal.getTime());
        return sdf.parse(strDate);
    }

    public static String getDate_TimeHeThongCurrent(){
        // Lấy thời gian hiện tại từ hệ thống
        LocalDateTime now = LocalDateTime.now();

        // Định dạng ngày giờ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Chuyển đổi thành chuỗi
        String dateTimeString = now.format(formatter);

        // In ra ngày giờ hiện tại dưới dạng chuỗi
        System.out.println("Original DateTime String: " + dateTimeString);

        // Tách chuỗi thành phần ngày và giờ
        String[] dateTimeParts = dateTimeString.split(" ");
        String datePart = dateTimeParts[0];
        String timePart = dateTimeParts[1];

        // Tách ngày thành năm, tháng, ngày
        String[] dateComponents = datePart.split("-");
        String year = dateComponents[0];
        String month = dateComponents[1];
        String day = dateComponents[2];

        // Tách giờ thành giờ, phút và giây
        String[] timeComponents = timePart.split(":");
        String hour = timeComponents[0];
        String minute = timeComponents[1];
        String second = timeComponents[2];

        // Nối các thành phần lại thành một chuỗi không có khoảng cách
        String result = day + month + year + hour + minute + second;

        return result;
    }
}
