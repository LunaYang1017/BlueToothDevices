package DeviceListView;

public class DeviceClass {

        private String bName; //蓝牙名称
        private String bAdress; //蓝牙地址

        public DeviceClass(String bName,String bAdress){
            this.bName = bName;
            this.bAdress = bAdress;
        }
        public String getbName(){
            return bName;
        }
        public String getbAdress(){
            return bAdress;
        }


}
