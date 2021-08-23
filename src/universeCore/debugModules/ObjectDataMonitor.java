package universeCore.debugModules;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;
import universeCore.util.handler.FieldHandler;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.ui;

public class ObjectDataMonitor{
  private static final ObjectMap<String, VarStructure> emptyVars = new ObjectMap<>(0);
  private ObjectMap<String, VarStructure> vars = emptyVars;
  private boolean initialized = false;
  
  public final SetFlagDialog setFlag = new SetFlagDialog();
  public final VarsDisplay varsDisplay = new VarsDisplay();
  
  public void setVars(ObjectMap<String, ObjectDataMonitor.VarStructure> vars){
    this.vars = vars;
    initialized = true;
  }
  
  public void unloadVars(){
    vars = emptyVars;
    initialized = false;
  }
  
  public DataDisplayTable creatDataTable(Object target){
    return new DataDisplayTable(target);
  }
  
  public ArrayPreviewTable creatArrayTable(Object array){
    if(array.getClass().isArray()) return new ArrayPreviewTable(array);
    return new ArrayPreviewTable(null){{
      clearChildren();
      add(Core.bundle.get("warning.non-array"));
    }};
  }
  
  public class DataDisplayTable extends Table{
    public final Object target;
    public final Seq<Field> targetData = new Seq<>();
    public final Seq<Method> testMethods = new Seq<>();
    public final VarsDisplay varsDisplay;
  
    private Runnable rebuild = () -> {};
    private String search = "";
    
    private Class<?> currentClazz;
    
    public DataDisplayTable(Object target){
      this.target = target;
      this.varsDisplay = new VarsDisplay();
      
      currentClazz = target.getClass();
      
      clearChildren();
      defaults().top().padTop(5).margin(0).grow();
      
      table(t -> {
        t.left();
        t.image(Icon.zoom);
        t.field("", str -> {
          search = str;
          rebuild.run();
        }).growX();
      }).fillX();
      
      row();
  
      button(Core.bundle.get("debugModule.dataMonitor.loadParentClass"), () -> {
        currentClazz = currentClazz.getSuperclass();
        rebuild.run();
      }).update(e -> {
        if(e.isDisabled()) e.setColor(Pal.gray);
      }).get().setDisabled(() -> currentClazz.getSuperclass() == null || currentClazz.getSuperclass() == Object.class);
  
      row();
  
      Table data = new Table();
      Table methods = new Table();
  
      add(data);
      row();
      add(methods);
  
      rebuild = () -> {
        try{
          Seq<Field> newField = getData(currentClazz);
          for(Field field : newField){
            if(!targetData.contains(field)) targetData.add(field);
          }
        }catch(IllegalAccessException e){
          Log.err(e);
        }
  
        Seq<Method> newMethod = getTestMethod(currentClazz);
        for(Method method : newMethod){
          if(!testMethods.contains(method)) testMethods.add(method);
        }
        
        data.clearChildren();
        methods.clearChildren();
        
        if(targetData.size > 0 || target.getClass().getSuperclass() != null){
          
          data.defaults().grow().left().top().margin(0).padLeft(5f);
          data.add(Core.bundle.get("misc.data")).color(Pal.accent).left();
          data.row();
          data.image().color(Pal.accent).colspan(3).height(4).growX();
  
          targetData.each(f -> {
            if(!search.equals("")){
              if(!f.getName().contains(search)) return;
            }
            Object object;
            try{
              object = f.get(target);
            }catch(IllegalAccessException e){
              Log.err(e);
              return;
            }
            data.row();
        
            TextButton button1 = new TextButton("[gray]flag");
            ObjectStructure structure = new ObjectStructure(object);
        
            button1.update(() -> {
              String key = vars.findKey(structure, true);
              button1.setText(Objects.requireNonNullElse(key, "[gray]flag"));
            });
        
            button1.clicked(() -> {
              String preString = button1.getText().toString();
              setFlag.show(button1.getLabel(), preString, structure);
            });
            data.table(t -> {
              t.defaults().left().grow().fill().margin(0);
              t.add(button1).width(120);
              t.add("[accent]" + Modifier.toString(f.getModifiers()) + " " + f.getType().getSimpleName().replace("[]", "[#]") + "[] " + f.getName()).padLeft(2);
            }).left().height(50f);
            
            Table table = new Table();
            buildObjectTable(target, f, object, table);
            
            data.add(table).grow().right();
          });
        }
    
        if(testMethods.size > 0){
          methods.defaults().grow().margin(0);
          methods.add(Core.bundle.get("misc.method")).color(Pal.accent).left();
          methods.row();
          methods.image().color(Pal.accent).colspan(3).height(4).growX();
          
          for(int i=0; i<testMethods.size; i++){
            Method method = testMethods.get(i);
            if(!search.equals("")){
              if(!method.getName().contains(search)) return;
            }
            MethodInvokeDialog methodInvoke = new MethodInvokeDialog(method, target);
            methods.row();
            Table t = new Table(i == 0? Tex.buttonEdge1: Tex.pane);
            t.defaults().left().pad(4f).grow();
            t.add(new StringJoiner(" ").add(method.getReturnType().getTypeName()).add(Modifier.toString(method.getModifiers())).add(method.getName()).toString()).color(Pal.accent).padBottom(0);
            t.row();
        
            StringBuilder parameter = new StringBuilder("(");
            boolean first = true;
            for(Class<?> clazz : method.getParameterTypes()){
              parameter.append(first? clazz.getName(): ", " + clazz.getName());
              first = false;
            }
            parameter.append(")");
        
            t.add(parameter.toString()).padTop(0);
        
            t.clicked(methodInvoke::show);
            methods.add(t).left().margin(4f);
          }
        }
      };
      
      rebuild.run();
    }
  
    private void buildObjectTable(Object target, Field f, Object object, Table table){
      if(object instanceof Number || object instanceof String || object instanceof Boolean || object == null){
        boolean isFinal = Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers());
        table.margin(0);
        table.defaults().top().grow().pad(0);
    
        Table button = new Table();
        Label text = new Label(object == null? "null": object.toString());
        button.add(text).get().setColor(isFinal? Pal.accent: Color.white);
    
        button.update(() -> {
          try{
            Object curr = f.get(target);
            text.setText(curr == null? "null": curr.toString());
          }catch(IllegalAccessException e){
            Log.err(e);
          }
        });
    
        button.clicked(() -> {
          if(isFinal) return;
          Object curr = null;
      
          try{
            curr = f.get(target);
          }catch(IllegalAccessException e){
            Log.err(e);
          }
      
          table.clearChildren();
      
          TextField modifier = new TextField(curr == null? "null": curr.toString());
          table.add(modifier).right();
          table.button(Core.bundle.get("misc.confirm"), () -> {
            String input = modifier.getText();
            
            setValue(f, target, input);
            buildObjectTable(target, f, object, table);
        
            table.clearChildren();
            table.add(button).minWidth(120).maxWidth(180);
          });
        });
        table.add(button).minWidth(120).maxWidth(180);
      }
      else if(object.getClass().isArray()){
        table.add(new ArrayPreviewTable(object)).grow();
      }
      else{
        TextButton unfold = new TextButton(Core.bundle.get("misc.unfold"));
        TextButton fold = new TextButton(Core.bundle.get("misc.fold"));
        
        table.defaults().top().fill().grow().pad(0);
    
        TextButton modify = new TextButton(Core.bundle.get("misc.modify"));
        modify.clicked(() -> {
          table.clearChildren();
          TextField input = table.field("", null).left().get();
          table.button(Core.bundle.get("misc.confirm"), () -> {
            VarStructure struct = vars.get(input.getText());
            Object obj = null;
            if(struct != null){
              obj = struct.get();
            }
        
            if(input.getText().startsWith("@")){
              FieldHandler.setValue(f, target, obj);
              buildObjectTable(target, f, object, table);
            }
            else if(input.getText().equals("null")){
              FieldHandler.setValue(f, target, null);
              buildObjectTable(target, f, object, table);
            }
        
            table.clearChildren();
            table.add(modify).right().height(50).minWidth(120).maxWidth(180);
            table.add(unfold).minWidth(120).maxWidth(180);
          }).right();
        });
    
        table.add(modify).right().height(50).minWidth(120).maxWidth(180);
        table.add(unfold).minWidth(120).maxWidth(180);
    
        unfold.clicked(() -> {
          table.clearChildren();
          table.add(fold).right().height(50).minWidth(120).maxWidth(180);
          table.table(t -> {
            t.table(Tex.button, child -> {
              child.right().add(new DataDisplayTable(object)).grow();
            }).padLeft(5f);
          });
        });
    
        fold.clicked(() -> {
          table.clearChildren();
          table.add(modify).right().height(50).expand().minWidth(120).maxWidth(180);
          table.add(unfold).minWidth(120).maxWidth(180);
        });
      }
    }
  }
  
  private class ArrayPreviewTable extends Table{
  
    public ArrayPreviewTable(Object array){
      TextButton unfold = new TextButton(Core.bundle.get("misc.unfold"));
      TextButton fold = new TextButton(Core.bundle.get("misc.fold"));
      Table table = new Table();
      table.defaults().top().fill().grow();
      table.add(unfold).right().height(50);
      
      unfold.clicked(() -> {
        table.clearChildren();
        table.add(fold).right().height(50);
        table.table(Tex.button, child -> {
          child.right().table(Tex.button, arr -> {
            arr.defaults().left();
            
            for(int index = 0; index< Array.getLength(array); index++){
              int tempIndex = index;
              Object item = Array.get(array, index);
              
              TextButton button1 = new TextButton("[gray]flag");
              ArrayStructure structure = new ArrayStructure(index, tempIndex);
              Interval timer = new Interval(2);
              button1.update(() -> {
                if(timer.get(1, 30f)){
                  String key = vars.findKey(structure, true);
                  button1.setText(Objects.requireNonNullElse(key, "[gray]flag"));
                }
              });
              
              button1.clicked(() -> {
                String preString = button1.getText().toString();
                setFlag.show(button1.getLabel(), preString, new ArrayStructure(array, tempIndex));
              });
              arr.table(t -> {
                t.defaults().grow().left();
                t.add(button1).width(120);
                t.add("[" + tempIndex + "]");
              }).height(50f);
              
              if(item instanceof Number || item instanceof Boolean || item instanceof String || item == null){
                Table itemBar = new Table();
                itemBar.margin(0);
                itemBar.defaults().left();
                
                Table button = new Table();
                Label text = new Label(item == null? "null": item.toString());
                button.add(text);
                
                button.update(() -> {
                  Object object = Array.get(array, tempIndex);
                  text.setText(object != null? object.toString(): "null");
                });
                button.clicked(() -> {
                  itemBar.clearChildren();
                  TextField modifier = new TextField(item == null? "null": item.toString());
                  itemBar.add(modifier).left();
                  
                  itemBar.button(Core.bundle.get("misc.confirm"), () -> {
                    try{
                      String input = modifier.getText();
                      
                      if(input.startsWith("@")){
                        Array.set(array, tempIndex, vars.get(input.replaceFirst("@", "")));
                      }
                      else Array.set(array, tempIndex, array.getClass().getComponentType().getMethod("valueOf", String.class).invoke(null, modifier.getText()));
                    }catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                      Log.err(e);
                    }
                    
                    itemBar.clearChildren();
                    itemBar.add(button).minWidth(120f);
                  }).right();
                });
                itemBar.add(button).minWidth(120f);
                arr.add(itemBar).right();
              }
              else if(item.getClass().isArray()){
                arr.add(new ArrayPreviewTable(item)).grow();
              }
              else{
                TextButton unfold1 = new TextButton(Core.bundle.get("misc.unfold"));
                TextButton fold1 = new TextButton(Core.bundle.get("misc.fold"));
                Table table1 = new Table();
                table1.defaults().top().fill().grow();
                table1.add(unfold1).right().height(50);
                
                unfold1.clicked(() -> {
                  table1.clearChildren();
                  table1.add(fold1).right().height(50);
                  table1.table(t -> {
                    t.table(Tex.button, cc -> {
                      cc.right().add(new DataDisplayTable(item)).grow();
                    }).padLeft(5f);
                  });
                });
                
                fold1.clicked(() -> {
                  table1.clearChildren();
                  table1.add(unfold1).right().height(50);
                });
                
                arr.add(table1).right();
              }
              
              arr.row();
            }
          }).grow();
        }).padLeft(5f);
      });
      
      fold.clicked(() -> {
        table.clearChildren();
        table.add(unfold).right().height(50);
      });
      
      add(table).grow().right();
    }
  }
  
  public class VarsDisplay extends BaseDialog{
    private final SetFlagDialog resetFlag = new SetFlagDialog(){
      @Override
      public void setValue(TextField field, Label text, String preString, VarStructure structure){
        super.setValue(field, text, preString, structure);
        if(!preString.equals(text.getText().toString())){
          vars.remove(preString);
        }
      }
    };
    
    public VarsDisplay(){
      super(Core.bundle.get("debugModule.dataMonitor.vars"));
      addCloseButton();
    }
    
    public void build(){
      cont.clearChildren();
      
      cont.table(all -> {
        all.defaults().height(50).grow();
        all.row();
        vars.each((s, structure) -> {
          Object object = structure.get();
          
          all.table(Tex.buttonTrans, t -> {
            t.defaults().grow().left().pad(0).margin(0);
      
            TextButton button = new TextButton(s);
            button.clicked(() -> {
              resetFlag.show(button.getLabel(), button.getText().toString(), structure);
            });
            t.add(button).width(120);
      
            if(object.getClass().isArray()){
              t.add(new ArrayPreviewTable(object));
            }
            else if(isBasicType(object.getClass())){
              t.add(object.toString());
            }
            else{
              TextButton unfold = new TextButton(Core.bundle.get("misc.unfold"));
              TextButton fold = new TextButton(Core.bundle.get("misc.fold"));
              Table table = new Table();
              table.defaults().top().fill().grow();
              table.add(unfold).right().height(50);
  
              unfold.clicked(() -> {
                table.clearChildren();
                table.add(fold).right().height(50).expand();
                table.table(t1 -> {
                  t1.table(Tex.button, child -> {
                    child.right().add(new DataDisplayTable(object)).grow();
                  }).padLeft(5f);
                });
              });
  
              fold.clicked(() -> {
                table.clearChildren();
                table.add(unfold).right().height(50).expand();
              });
  
              t.pane(table).grow().right();
            }
            
            t.button(Icon.copy, () -> Core.app.setClipboardText("@" + button.getText())).right().size(50).padRight(0).marginRight(0);
      
            t.button(Icon.trash, () -> {
              new BaseDialog(Core.bundle.get("debugModule.dataMonitor.varsDelete")){
                @Override
                public Dialog show(){
                  cont.table(table -> {
                    table.defaults().grow();
                    table.table(Tex.button, t -> {
                      t.add(Core.bundle.format("debugModule.dataMonitor.varsDeleteConfirm", s + "(" + object.toString() + ")"));
                    }).height(250);
                    table.row();
                    table.table(t -> {
                      t.defaults().grow();
                      t.button(Core.bundle.get("misc.cancel"), this::hide);
                      t.button(Core.bundle.get("misc.confirm"), () -> {
                        hide();
                        vars.remove(s);
                        build();
                      });
                    }).height(60);
                  }).size(400, 300);
      
                  return super.show();
                }
              }.show();
            }).size(50).right().padRight(0).marginRight(0);
          }).fillY();
          all.row();
        });
      }).width(900f);
    }
  
    @Override
    public Dialog show(){
      if(initialized){
        build();
        return super.show();
      }
      else new BaseDialog(Core.bundle.get("debugModule.dataMonitor.varsNull")){
        {
          cont.add("debugModule.dataMonitor.monitorNoTarget");
          addCloseButton();
        }
      }.show();
      return null;
    }
  }
  
  private class MethodInvokeDialog extends BaseDialog{
    final Method method;
    final Object target;
    
    private Object returnValue;
    StringBuilder stackTrace;
    private boolean tested, error;
    
    public MethodInvokeDialog(Method method, Object target){
      super(Core.bundle.format("debugModule.dataMonitor.invokeMethod", method.getName()));
      addCloseButton();
      buttons.button(Core.bundle.get("debugModule.dataMonitor.vars"), varsDisplay::show);
      this.method = method;
      this.target = target;
      build();
    }
    
    public void build(){
      Table table = new Table();
      AtomicBoolean invoked = new AtomicBoolean(false);
      table.defaults().minHeight(120f).minWidth(360).grow();
      table.table(Tex.button, t -> {
        t.defaults().grow();
        
        t.add(new StringJoiner(" ").add(method.getReturnType().toString()).add(Modifier.toString(method.getModifiers())).add(method.getName()).toString());
        t.row();
        
        StringBuilder parameterStr = new StringBuilder();
        boolean first = true;
        for(Class<?> clazz: method.getParameterTypes()){
          parameterStr.append(first? clazz.getName(): ", " + clazz.getName());
          first = false;
        }
        t.add(Core.bundle.format("debugModule.dataMonitor.methodParameter", parameterStr.toString()));
        
        t.row();
        TextField field = new TextField();
        t.add(Core.bundle.get("misc.typeParams")).color(Pal.gray);
        t.row();
        t.add(field).padTop(10f);
        
        t.row();
        TextButton b = t.button(Core.bundle.get("misc.methodInvoke"), () -> {
          invoked.set(true);
          try{
            String[] params = field.getText().split(",");
            Object[] parameter = new Object[(params.length == 1 && params[0].equals(""))? 0: params.length];
            Class<?>[] paramType = method.getParameterTypes();
            
            if(!(params.length == 1 && params[0].equals("")) && params.length != paramType.length){
              ui.showErrorMessage(new IllegalArgumentException("Incorrect number of parameters(input: " + params.length + ", require: " + paramType.length).toString());
              return;
            }
            
            if(paramType.length > 0) for(int i=0; i<params.length; i++){
              
              if(params[i].startsWith("@")){
                parameter[i] = vars.get(params[i].replaceFirst("@", "")).get();
              }
              else{
                try{
                  if(isBasicType(paramType[i])){
                    String typeClassName = !paramType[i].getName().equals("int")? paramType[i].getName(): "Integer";
                    parameter[i] = Class.forName("java.lang." + typeClassName.substring(0, 1).toUpperCase(Locale.ROOT) + typeClassName.substring(1)).getDeclaredMethod("valueOf", String.class).invoke(null, params[i]);
                  }
                }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
                  Log.err(e);
                }
              }
            }
            Log.info("invoking method: " + method.getName() + ",with param:" + Arrays.toString(parameter));
            returnValue = method.invoke(target, parameter);
            tested = true;
            error = false;
          }
          catch(Throwable e){
            returnValue = e.getStackTrace();
            error = true;
            stackTrace = new StringBuilder(e + "\n");
            for(StackTraceElement err : (StackTraceElement[]) returnValue){
              stackTrace.append(err).append("\n");
            }
          }
        }).get();
        
        keyDown(KeyCode.enter, b::fireClick);
        keyDown(KeyCode.escape, this::hide);
      });
      table.row();
      table.table(Tex.button).update(t -> {
        if(!invoked.get()) return;
        invoked.set(false);
        t.clearChildren();
        if(method.getReturnType() != void.class){
          TextButton button = new TextButton("[gray]flag");
          button.clicked(() -> {
            String preString = button.getText().toString();
            setFlag.show(button.getLabel(), preString, new ObjectStructure(returnValue));
          });
          t.add(button).left();
          
          if(!tested){
            t.add(Core.bundle.get("debugModule.dataMonitor.defaultReturn"));
            return;
          }
          if(error){
            t.add(stackTrace);
            return;
          }
          if(returnValue instanceof Number || returnValue instanceof String || returnValue instanceof Boolean || returnValue == null){
            t.add(returnValue == null? "null": returnValue.toString()).right();
          }
          else if(returnValue.getClass().isArray()){
            t.add(new ArrayPreviewTable(returnValue)).grow();
          }
          else{
            TextButton unfold = new TextButton(Core.bundle.get("misc.unfold"));
            TextButton fold = new TextButton(Core.bundle.get("misc.fold"));
            Table table1 = new Table();
            table1.defaults().top().fill().grow();
            table1.add(unfold).right().height(50);
            
            unfold.clicked(() -> {
              table1.clearChildren();
              table1.add(fold).right().height(50).expand();
              table1.table(t1 -> {
                t1.table(Tex.button, child -> {
                  child.right().add(new DataDisplayTable(returnValue)).grow();
                }).padLeft(5f);
              });
            });
            
            fold.clicked(() -> {
              table1.clearChildren();
              table1.add(unfold).right().height(50).expand();
            });
            
            t.add(table1).right();
          }
        }
        else{
          t.add("").update(e -> {
            e.setText(error? stackTrace: "void");
            e.setColor(error? Color.crimson: Color.white);
          }).right();
        }
      });
      
      cont.add(table);
    }
  }
  
  public class SetFlagDialog extends BaseDialog{
    public SetFlagDialog(){
      super(Core.bundle.get("debugModule.dataMonitor.setFlag"));
    }
    
    public void build(Label text, String preString, VarStructure structure){
      cont.clearChildren();
      
      Table table = new Table();
      TextField field = new TextField(preString);
      table.defaults().margin(0).pad(0).grow();
      table.table(Tex.button, t -> {
        t.add(Core.bundle.get("debugModule.dataMonitor.inputFlagField")).color(Pal.gray);
        t.row();
        t.add(field);
      }).height(200);
      table.row();
      table.table(t -> {
        t.defaults().grow();
        t.button(Core.bundle.get("misc.cancel"), this::hide);
        t.button(Core.bundle.get("misc.confirm"), () -> setValue(field, text, preString, structure));
      });
  
      keyDown(KeyCode.enter, () -> setValue(field, text, preString, structure));
      keyDown(KeyCode.escape, this::hide);
      cont.add(table).size(400, 300);
    }
    
    public void setValue(TextField field, Label text, String preString, VarStructure structure){
      String str = field.getText();
      if(field.getText().equals(preString)) return;
      if(str.replace(" ", "").equals("")){
        text.setText("[gray]flag");
        vars.remove(preString);
      }
      else{
        text.setText(str);
        vars.put(str, structure);
      }
      hide();
    }
    
    public void show(Label text, String preString, VarStructure structure){
      if(!initialized){
        ui.showErrorMessage(Core.bundle.get("warning.noVarsSetError"));
        return;
      }
      build(text, preString, structure);
      show();
    }
  }
  
  public interface VarStructure{
    Object get();
  }
  
  public static class ArrayStructure implements VarStructure{
    private final Object array;
    private final int index;
    private final boolean isBasicType;
    
    private final ObjectStructure proxy;
    
    public ArrayStructure(Object array, int index){
      this.array = array;
      this.index = index;
      Class<?> clazz = array.getClass().arrayType();
      isBasicType = isBasicType(clazz);
      
      if(isBasicType){
        proxy = new ObjectStructure(Array.get(array, index));
      }
      else proxy = null;
    }
    
    @Override
    public Object get(){
      return isBasicType? proxy.get(): Array.get(array, index);
    }
  }
  
  public static class ObjectStructure implements VarStructure{
    private final Object object;
    private final boolean isNull;
    
    public ObjectStructure(@Nullable Object object){
      this.object = object;
      this.isNull = object == null;
    }
    
    @Override
    public Object get(){
      return isNull? "null": object;
    }
  }
  
  private void setValue(Field field, Object target, String value){
    Class<?> clazz = isInternalBasicType(field.getType())? castToDefaultType(field.getType()): field.getType();
    
    if(value.equals("null")){
      FieldHandler.setValue(field, target, null);
      return;
    }
    
    if(value.startsWith("@")){
      Object result = vars.get(value.replaceFirst("@", ""));
      FieldHandler.setValue(field, target, result);
    }
    else{
      try{
        if(!isBasicType(clazz) || clazz == null){
          Vars.ui.showErrorMessage("Can not set a Non-basic type by normal string!\n(class:" + clazz + ")");
          return;
        }
        Method maker = clazz.getMethod("valueOf", String.class);
        FieldHandler.setValue(field, target, maker.invoke(null, value));
      }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
        Log.err(e);
      }
    }
  }
  
  public static Seq<Field> getData(Class<?> clazz) throws IllegalAccessException{
    Seq<Field> result = new Seq<>();
    Field[] fields = clazz.getDeclaredFields();
    for(Field field: fields){
      field.setAccessible(true);
      result.add(field);
    }
    return result;
  }
  
  public static Seq<Method> getTestMethod(Class<?> clazz){
    Seq<Method> methods = new Seq<>(clazz.getDeclaredMethods());
    methods.each(e -> e.setAccessible(true));
    return methods;
  }
  
  private static boolean isBasicType(Class<?> clazz){
    return clazz == Integer.class || clazz == Float.class ||
      clazz == String.class || clazz == Boolean.class ||
      clazz == Byte.class || clazz == Short.class ||
      clazz == Long.class || clazz == Double.class || isInternalBasicType(clazz);
  }
  
  private static boolean isInternalBasicType(Class<?> clazz){
    return clazz == int.class || clazz == float.class ||
      clazz == boolean.class ||
      clazz == byte.class || clazz == short.class ||
      clazz == long.class || clazz == double.class;
  }
  
  private static Class<?> castToDefaultType(Class<?> clazz) throws RuntimeException{
    if(isInternalBasicType(clazz)){
      try{
        return Class.forName("java.lang." + (clazz.getTypeName().equals("int")? "Integer": clazz.getTypeName().substring(0, 1).toUpperCase(Locale.ROOT) + clazz.getTypeName().substring(1)));
      }catch(ClassNotFoundException e){
        Log.err(e);
        return null;
      }
    }
    else throw new RuntimeException("Try to cast non-internal type to default class");
  }
}
