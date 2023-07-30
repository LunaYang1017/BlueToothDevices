package DeviceListView;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.frombuttontoconnect.R;

import java.util.List;

public class DeviceAdapter extends  ArrayAdapter<BluetoothDevice>{//



    /*private List<BluetoothClass>mBondedDevice;
    * private List<BluetoothClass>mfindDeviceList;*/

    private int resourceId;

    public DeviceAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<BluetoothDevice> objects) {
        super(context, textViewResourceId,objects);
        resourceId=textViewResourceId;
    }

   /* public DeviceAdapter(@NonNull Context context,  int textViewResourceId, @NonNull List<BluetoothDevice> objects) {
        super(context,  textViewResourceId);
        resourceId=textViewResourceId;
    }*/


    /*public  static class ViewHolder  {
        public TextView textView1,textView2;
    }*/


   public View getView(int position, View convertView, ViewGroup parent){
        //BluetoothDevice device = getItem(position);
        BluetoothDevice device = getItem(position);
        //ViewHolder holder=null;
       View view=convertView;

        if (view == null) {//如果可复用的视图是空的，就新建一个视图

            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);//context上下文，语境，环境：）
           /* holder=new ViewHolder();                                                                        //上一行除了最后一个括号，前面都不用改，false不知道什么意思
            holder.textView1=convertView.findViewById(R.id.textView1);
            holder.textView2=convertView.findViewById(R.id.textView2);
            convertView.setTag(holder);*/
        }
       // else {
            /*holder= (ViewHolder) convertView.getTag();}
            //以下是赋值
            holder.textView1.setText(device.getbName());
            holder.textView2.setText(device.getbAddress());*/
       TextView textView1 = (TextView) view.findViewById(R.id.textView1);
       TextView textView2 = (TextView) view.findViewById(R.id.textView2);
       textView1.setText(device.getName());
       textView2.setText(device.getAddress());

        return view;//一个当前生成的视图
    }
    /*参数说明：by chatGPT

position: 当前视图在适配器数据集中的位置。
convertView: 可重用的视图，可以是之前已经创建的视图（进行复用）或者为 null（需要创建新视图）。
parent: 父视图，即列表或网格视图。
获取数据项：
从适配器的数据集中获取指定位置 position 处的数据对象 device。

视图复用：

如果 convertView 是 null，表示没有可重用的视图，需要创建一个新的视图。
使用 LayoutInflater 根据指定的布局资源 resourceId 创建视图对象 convertView。/这里resourceID是DeviceClass里面的positionID
创建一个 ViewHolder 对象 holder，并将视图中的子视图（比如文本框）与 ViewHolder 中的成员变量进行关联。
将 holder 对象通过 setTag() 方法附加到 convertView 上，以便稍后可以通过 convertView.getTag() 方法获取到该对象。
如果 convertView 不为 null，表示存在可重用的视图，直接通过 getTag() 方法获取之前附加的 ViewHolder 对象。
赋值：

通过 holder.textView1 和 holder.textView2 分别获取到 convertView 中的两个文本框控件。
使用 device 对象的方法 getbName() 和 getbAdress() 获取相应的数据。
将获取到的数据分别设置到 textView1 和 textView2 的文本属性中。
返回视图：

返回经过赋值后的 convertView 视图对象。
该函数的目的是在每个列表或网格项中显示与数据集中的每个对象相关的信息。通过检查 convertView 是否为空，可以避免频繁的视图创建和销毁操作，提高了列表或网格视图的性能。
同时，使用 ViewHolder 对象来缓存视图中的子视图，避免了多次调用 findViewById 的开销，进一步提高了性能。 最后，赋值阶段将数据显示到适配器的视图中，完成了数据与视图的绑定过程。*/
}
