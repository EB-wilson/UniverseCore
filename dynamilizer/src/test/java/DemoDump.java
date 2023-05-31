import org.objectweb.asm.*;

public class DemoDump implements Opcodes{

  public static byte[] dump() throws Exception{
      ClassWriter classWriter = new ClassWriter(0);
      FieldVisitor fieldVisitor;
      RecordComponentVisitor recordComponentVisitor;
      MethodVisitor methodVisitor;
      AnnotationVisitor annotationVisitor0;

      classWriter.visit(V17, ACC_PUBLIC|ACC_SUPER, "Demo", null, "java/lang/Object", null);

      classWriter.visitSource("Demo.java", null);

      {
        annotationVisitor0 = classWriter.visitAnnotation("LInter;", false);
        annotationVisitor0.visit("value", Type.getType("Ljava/lang/Object;"));
        annotationVisitor0.visitEnd();
      }
      {
        fieldVisitor = classWriter.visitField(ACC_PUBLIC, "name", "Ljava/lang/String;", null, null);
        fieldVisitor.visitEnd();
      }
      {
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(5, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(6, label1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(PUTFIELD, "Demo", "name", "Ljava/lang/String;");
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(7, label2);
        methodVisitor.visitInsn(RETURN);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLocalVariable("this", "LDemo;", null, label0, label3, 0);
        methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label3, 1);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
      }
      {
        methodVisitor = classWriter.visitMethod(ACC_PUBLIC|ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(10, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn(Type.getType("LDemo;"));
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(11, label1);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitInsn(ICONST_3);
        methodVisitor.visitInsn(ICONST_4);
        methodVisitor.visitInsn(ICONST_5);
        methodVisitor.visitMultiANewArrayInsn("[[[Ljava/lang/Object;", 3);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(12, label2);
        methodVisitor.visitInsn(RETURN);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLocalVariable("args", "[Ljava/lang/String;", null, label0, label3, 0);
        methodVisitor.visitMaxs(4, 1);
        methodVisitor.visitEnd();
      }
      classWriter.visitEnd();

      return classWriter.toByteArray();
    }
}