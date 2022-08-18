package universecore.androidcore.classes;

import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.DexFile;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import dynamilize.classmaker.code.IMethod;
import dynamilize.classmaker.code.IOperate;
import org.objectweb.asm.Opcodes;
import universecore.util.handler.MethodHandler;

import static dynamilize.classmaker.ClassInfo.STRING_TYPE;

public class DexGenerator extends ASMGenerator{

  private static final ClassInfo<StringBuilder> BUILDER_TYPE = ClassInfo.asType(StringBuilder.class);
  private static final IMethod<StringBuilder, String> TO_STRING = BUILDER_TYPE.getMethod(STRING_TYPE, "toString");

  public DexGenerator(ByteClassLoader classLoader){
    super(classLoader, Opcodes.V1_8);
  }

  @Override
  public byte[] genByteCode(ClassInfo<?> classInfo){
    byte[] byteCode = super.genByteCode(classInfo);

    DexOptions dexOptions = new DexOptions();
    DexFile dexFile = new DexFile(dexOptions);
    DirectClassFile classFile = MethodHandler.newInstanceDefault(
        DirectClassFile.class,
        byteCode,
        classInfo.internalName() + ".class"
    );
    MethodHandler.invokeDefault(classFile, "setAttributeFactory");
    classFile.getMagic();
    DxContext context = new DxContext();

    dexFile.add(MethodHandler.invokeDefault(CfTranslator.class, "translate",
        context,
        classFile,
        new CfOptions(),
        dexOptions,
        dexFile)
    );

    return MethodHandler.invokeDefault(dexFile, "toDex");
  }

  @Override
  public void visitOperate(IOperate<?> operate){
    if(operate.leftOpNumber().type() == STRING_TYPE || operate.rightOpNumber().type() == STRING_TYPE){
      IMethod<StringBuilder, StringBuilder> left, right;
      left = BUILDER_TYPE.getMethod(BUILDER_TYPE, "append", operate.leftOpNumber().type());
      right = BUILDER_TYPE.getMethod(BUILDER_TYPE, "append", operate.rightOpNumber().type());

      methodVisitor.visitTypeInsn(NEW, BUILDER_TYPE.internalName());
      methodVisitor.visitInsn(DUP);
      methodVisitor.visitMethodInsn(
          INVOKESPECIAL,
          BUILDER_TYPE.internalName(),
          "<init>",
          BUILDER_TYPE.getConstructor().typeDescription(),
          false
      );

      methodVisitor.visitVarInsn(
          getLoadType(operate.leftOpNumber().type()),
          localIndex.get(operate.leftOpNumber().name())
      );
      methodVisitor.visitMethodInsn(
          INVOKEVIRTUAL,
          BUILDER_TYPE.internalName(),
          "append",
          left.typeDescription(),
          false
      );
      methodVisitor.visitVarInsn(
          getLoadType(operate.rightOpNumber().type()),
          localIndex.get(operate.rightOpNumber().name())
      );
      methodVisitor.visitMethodInsn(
          INVOKEVIRTUAL,
          BUILDER_TYPE.internalName(),
          "append",
          right.typeDescription(),
          false
      );
      methodVisitor.visitMethodInsn(
          INVOKEVIRTUAL,
          BUILDER_TYPE.internalName(),
          "toString",
          TO_STRING.typeDescription(),
          false
      );
      methodVisitor.visitVarInsn(ASTORE, localIndex.get(operate.resultTo().name()));
    }
    else super.visitOperate(operate);
  }
}
