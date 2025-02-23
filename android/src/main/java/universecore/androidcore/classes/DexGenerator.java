package universecore.androidcore.classes;

import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.DexFile;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import dynamilize.classmaker.CodeBlock;
import dynamilize.classmaker.code.IMethod;
import dynamilize.classmaker.code.IOperate;
import org.objectweb.asm.Opcodes;

import java.io.IOException;

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
    DirectClassFile classFile = new DirectClassFile(
        byteCode,
        classInfo.internalName() + ".class",
        false
    );
    classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
    classFile.getInterfaces();
    DxContext context = new DxContext();

    dexFile.add(CfTranslator.translate(
        context,
        classFile,
        null,
        new CfOptions(),
        dexOptions,
        dexFile
    ));

    try {
      return dexFile.toDex(null, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void visitOperate(IOperate<?> operate){
    if(operate.leftOpNumber().type() == STRING_TYPE || operate.rightOpNumber().type() == STRING_TYPE){
      int leftInd = operate.leftOpNumber() instanceof CodeBlock.StackElem?
          localIndex.computeIfAbsent("$leftCache$", e -> localIndex.size()):
          localIndex.get(operate.leftOpNumber().name());

      int rightInd = operate.rightOpNumber() instanceof CodeBlock.StackElem?
          localIndex.computeIfAbsent("$rightCache$", e -> localIndex.size()):
          localIndex.get(operate.rightOpNumber().name());

      if (operate.leftOpNumber() instanceof CodeBlock.StackElem<?>){
        methodVisitor.visitVarInsn(
            getStoreType(STRING_TYPE),
            leftInd
        );
      }
      if (operate.rightOpNumber() instanceof CodeBlock.StackElem<?>){
        methodVisitor.visitVarInsn(
            getStoreType(STRING_TYPE),
            rightInd
        );
      }

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
          leftInd
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
          rightInd
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
