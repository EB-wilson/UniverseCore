package universecore.util.handler;

import dynamilize.DynamicMaker;
import dynamilize.classmaker.AbstractClassGenerator;
import universecore.util.classes.AbstractFileClassLoader;

public interface ClassHandler{
  AbstractClassGenerator getGenerator();

  DynamicMaker getDynamicMaker();

  AbstractFileClassLoader currLoader();

  void finishGenerate();
}
