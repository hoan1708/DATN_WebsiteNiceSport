const token = 'd9d54289-e5c8-11ee-a2c1-ca2feb4b63fa';
const config = {
    headers: {
        'token': token
    }
};

const callApiProvince = (api) => {
    return axios.get(api, config)
        .then((response) => {
            if (response.data && response.data.code === 200 && response.data.data) {
                const provinceId = document.getElementById("tinh_thanhPho").value;
                const province = response.data.data.find(province => province.ProvinceID == provinceId);

                if (province) {
                    console.log("Value của tỉnh thành phố: " + province.ProvinceName);
                    document.getElementById("tinh_thanhPho").value = province.ProvinceName;
                } else {
                    console.error("Province ID not found in response data");
                }
            } else {
                console.error("Response data format is incorrect or data is missing");
            }
        })
        .catch((error) => {
            console.error("Error calling API:", error);
        });
}

const tinhThanhPhoElement = document.getElementById("tinh_thanhPho");
if (tinhThanhPhoElement) {
    const provinceId = tinhThanhPhoElement.value;
    if (provinceId) {
        callApiProvince("https://online-gateway.ghn.vn/shiip/public-api/master-data/province?token=" + token);
    } else {
        console.error("Province ID is missing or invalid");
    }
} else {
    console.error("Element with ID 'tinh_thanhPho' not found");
}

const callApiDistrict = (provinceId, districtId) => {
    const api = `https://online-gateway.ghn.vn/shiip/public-api/master-data/district?province_id=${provinceId}`;

    return axios.get(api, config)
        .then((response) => {
            if (response.data && response.data.code === 200 && response.data.data) {
                const districts = response.data.data;
                console.log("Districts:", districts);

                const district = districts.find(d => d.DistrictID == districtId && d.ProvinceID == provinceId);

                if (district) {
                    console.log("District name:", district.DistrictName);
                    document.getElementById("quan_huyen").value = district.DistrictName;
                } else {
                    console.error("District not found with the given DistrictID and ProvinceID");
                }
            } else {
                console.error("Response data format is incorrect or data is missing");
            }
        })
        .catch((error) => {
            console.error("Error calling API:", error);
        });
}

const districtIdElement = document.getElementById("quan_huyen");

if (tinhThanhPhoElement && districtIdElement) {
    const provinceId = tinhThanhPhoElement.value;
    const districtId = districtIdElement.value;

    if (provinceId && districtId) {
        callApiDistrict(provinceId, districtId);
    } else {
        console.error("Province ID or District ID is missing or invalid");
    }
} else {
    console.error("Element with ID 'tinh_thanhPho' or 'quan_huyen' not found");
}

const callApiWard = (wardCode, districtId) => {
    const api = `https://online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=${districtId}`;

    return axios.get(api, config)
        .then((response) => {
            if (response.data && response.data.code === 200 && response.data.data) {
                const wards = response.data.data;
                console.log("Wards:", wards);

                const ward = wards.find(w => w.DistrictID == districtId && w.WardCode == wardCode);
                if (ward) {
                    console.log("Ward Name:", ward.WardName);
                    document.getElementById("phuong_xa").value = ward.WardName;
                } else {
                    console.error("Ward not found with the given DistrictID and WardCode");
                }
            } else {
                console.error("Response data format is incorrect or data is missing");
            }
        })
        .catch((error) => {
            console.error("Error calling API:", error);
        });
}

const wardElement = document.getElementById("phuong_xa");

if (wardElement && districtIdElement) {
    const wardCode = wardElement.value;
    const districtId = districtIdElement.value;

    if (wardCode && districtId) {
        callApiWard(wardCode, districtId);
    } else {
        console.error("Ward Code or District ID is missing or invalid");
    }
} else {
    console.error("Element with ID 'phuong_xa' or 'quan_huyen' not found");
}
