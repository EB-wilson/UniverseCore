package universecore.androidcore.classes.dexmaker;

import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import dynamilize.classmaker.code.IClass;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class DexClassInfo{
  private final IClass<?> typeInfo;

  private final List<DexFieldInfo> fields = new ArrayList<>();
  private final List<DexMethodInfo> methods = new ArrayList<>();

  private ClassDefItem item;

  public DexClassInfo(IClass<?> typeInfo){
    this.typeInfo = typeInfo;
  }

  public void addField(DexFieldInfo field){
    fields.add(field);
  }

  public void addMethod(DexMethodInfo method){
    methods.add(method);
  }

  public ClassDefItem toItem(){
    if(item != null) return item;

    List<IClass<?>> interfaces = typeInfo.interfaces();

    StdTypeList list = new StdTypeList(interfaces.size());

    for(int i = 0; i < interfaces.size(); i++){
      list.set(i, Type.intern(interfaces.get(i).realName()));
    }

    item = new ClassDefItem(
        new CstType(Type.intern(typeInfo.realName())),
        typeInfo.modifiers(),
        new CstType(Type.intern(typeInfo.superClass().realName())),
        list,
        null
    );

    for(DexFieldInfo field: fields){
      if(Modifier.isStatic(field.modifiers())){
        item.addStaticField(field.toItem(), field.getConstant());
      }
      item.addInstanceField(field.toItem());
    }
    for(DexMethodInfo method: methods){
      if(Modifier.isPrivate(method.modifiers()) || method.name().equals("<init>")){
        item.addDirectMethod(method.toItem());
      }
      else{
        item.addVirtualMethod(method.toItem());
      }
    }

    return item;
  }
}
