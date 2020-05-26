package pers.mine.jts_download;

/**
 * @author wangxiaoqiang
 * @description TODO
 * @create 2020-03-10 11:23
 */
public class Test {
    public static void main(String[] args) {
//        String text = "thisdate,vendor_id,fancy_os_id,pack_name,req_slot_type,req_slot_width,req_slot_height,req_allowed_creative_type,vendor_id,geo_code,fancy_id,ex_uid,req_slot_type,req_slot_width,req_slot_height,fancy_media_cate,is_mob,network_scene,request_id,url,page_category,fancy_device_type_id,title,media_keyword,pack_name,refer,slot_id,direct_deal_id,vendor_id,ex_uid,req_slot_type,title,pack_name,is_app,thisdate,pack_name,ex_uid,vendor_id,thisdate,idfa,idfa_hash,pack_name,vendor_id,imei,imei_hash";
//        List<String> strings = Arrays.asList(text.split(","));
//        Set<String> set = new HashSet<>(strings);
//        for (String s : set) {
//            System.out.println(s);
//        }

//        for (int i = 2; i < 3; i++) {
//            System.out.println(i);
//        }
//
//        int[] a = {1, 2};
//        int b = 3;
//        a[0] = b;
//        b = 5;
//        System.out.println(a[0]);
//        System.out.println(finTest());
        String dateTimeStr = "2019-12-30 18:10:00";
        int orderId = 1;
        int vendorId = 2;
        int dspId = 3;
        String slotTagId = "'11','12'";
        String ftxQttSaasKey = "'" + dateTimeStr.substring(0, 10) + "'," + dateTimeStr.substring(11, 13) + "," + "'" + dateTimeStr + "'," + orderId + "," + vendorId + "," + dspId + "," + slotTagId;
        System.out.println(ftxQttSaasKey);
    }

    public static String finTest() {
        String t = "hello";
        try {
            return (t + System.currentTimeMillis());
        } finally {
            t = "hello world";
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(t + System.currentTimeMillis());
            return (t + System.currentTimeMillis());
        }
    }

}
