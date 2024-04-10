package universecore.util.handler;

import dynamilize.DynamicMaker;
import dynamilize.classmaker.AbstractClassGenerator;
import universecore.util.classes.AbstractFileClassLoader;

public interface ClassHandler{
  ClassHandler newInstance(Class<?> modMain);

  AbstractClassGenerator getGenerator();

  DynamicMaker getDynamicMaker();

  AbstractFileClassLoader currLoader();

  void finishGenerate();
}
