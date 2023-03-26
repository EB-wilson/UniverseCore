package universecore.annotations;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings({"PatternVariableCanBeUsed", "EnhancedSwitchMigration", "TextBlockMigration"})
@AutoService(Processor.class)
public class ImportUNCProcessor extends BaseProcessor{
  private static final String STATUS_FIELD = "$status$";

  private static final String code = "{\n" +
                                     "  String $libVersionValue = \"0.0.0\";\n" +
                                     "  {\n" +
                                     "    final arc.struct.ObjectMap<String, String> bundles = arc.struct.ObjectMap.of($bundles);\n" +
                                     "    arc.files.Fi[] $modsFiles = arc.Core.settings.getDataDirectory().child(\"mods\").list();\n" +
                                     "    arc.files.Fi $libFileTemp = null;\n" +
                                     "    arc.files.Fi $modFile = null;\n" +
                                     "\n" +
                                     "    java.util.concurrent.atomic.AtomicBoolean $disabled = new java.util.concurrent.atomic.AtomicBoolean(false);\n" +
                                     "    for (arc.files.Fi $file : $modsFiles) {\n" +
                                     "      if ($file.isDirectory() || (!$file.extension().equals(\"jar\") && !$file.extension().equals(\"zip\"))) continue;\n" +
                                     "\n" +
                                     "      try{\n" +
                                     "        arc.files.Fi $zipped = new arc.files.ZipFi($file);\n" +
                                     "        arc.files.Fi $modManifest = $zipped.child(\"mod.hjson\");\n" +
                                     "        if ($modManifest.exists()) {\n" +
                                     "          arc.util.serialization.Jval $fest = arc.util.serialization.Jval.read($modManifest.readString());\n" +
                                     "          String $name = $fest.get(\"name\").asString();\n" +
                                     "          String $version = $fest.get(\"version\").asString();\n" +
                                     "          if ($name.equals(\"universe-core\")) {\n" +
                                     "            $libFileTemp = $file;\n" +
                                     "            $libVersionValue = $version;\n" +
                                     "          }\n" +
                                     "          else if ($fest.has(\"main\") && $fest.getString(\"main\").equals($className.class.getName())){\n" +
                                     "            $modFile = $file;\n" +
                                     "          }\n" +
                                     "        }\n" +
                                     "      }catch(Throwable e){\n" +
                                     "        continue;\n" +
                                     "      }\n" +
                                     "\n" +
                                     "      if ($modFile != null && $libFileTemp != null) break;\n" +
                                     "    }\n" +
                                     "\n" +
                                     "    assert $modFile != null;\n" +
                                     "\n" +
                                     "    arc.func.Intf<String> $versionValid = v -> {\n" +
                                     "      String[] $lib = v.split(\"\\\\.\");\n" +
                                     "      String[] $req = \"$requireVersion\".split(\"\\\\.\");\n" +
                                     "\n" +
                                     "      if (Integer.parseInt($lib[0]) > Integer.parseInt($req[0])) return 2;\n" +
                                     "      for (int i = 1; i < $lib.length; i++) {\n" +
                                     "        if (Integer.parseInt($lib[i]) > Integer.parseInt($req[i])) return 0;\n" +
                                     "        if (Integer.parseInt($lib[i]) < Integer.parseInt($req[i])) return 1;\n" +
                                     "      }\n" +
                                     "      return 0;\n" +
                                     "    };\n" +
                                     "    arc.Events.on(mindustry.game.EventType.ClientLoadEvent.class, e -> {\n" +
                                     "      arc.util.Time.run(1, () -> {\n" +
                                     "        arc.Core.settings.remove(\"unc-checkFailed\");\n" +
                                     "        arc.Core.settings.remove(\"unc-warningShown\");\n" +
                                     "      });\n" +
                                     "    });\n" +
                                     "    Runtime.getRuntime().addShutdownHook(new Thread(() -> {\n" +
                                     "      arc.Core.settings.remove(\"unc-checkFailed\");\n" +
                                     "      arc.Core.settings.remove(\"unc-warningShown\");\n" +
                                     "    }));\n" +
                                     "\n" +
                                     "    final arc.files.Fi $libFile = $libFileTemp;\n" +
                                     "    final String $libVersion = $libVersionValue;\n" +
                                     "\n" +
                                     "    final boolean $upgrade = $versionValid.get($libVersion) == 1;\n" +
                                     "    final boolean $requireOld = $versionValid.get($libVersion) == 2;\n" +
                                     "    if (mindustry.Vars.mods.getMod(\"universe-core\") == null || $upgrade || !arc.Core.settings.getBool(\"mod-universe-core-enabled\", true)) {\n" +
                                     "      if ($libFile == null || !$libFile.exists() || $upgrade || !arc.Core.settings.getBool(\"mod-universe-core-enabled\", true)) {\n" +
                                     "        arc.util.io.PropertiesUtils.load(arc.Core.bundle.getProperties(), new java.io.StringReader(bundles.get(arc.Core.bundle.getLocale().toString(), bundles.get(\"\"))));\n" +
                                     "\n" +
                                     "        String $curr = arc.Core.settings.getString(\"unc-checkFailed\", \"\");\n" +
                                     "        $curr += $modFile.path() + \"::\";\n" +
                                     "        if (!arc.Core.settings.getBool(\"mod-universe-core-enabled\", true)){\n" +
                                     "          $curr += \"dis\";\n" +
                                     "          $status$ = 1;\n" +
                                     "          $disabled.set(true);\n" +
                                     "        }\n" +
                                     "        else if ($libFile == null){\n" +
                                     "          $curr += \"none\";\n" +
                                     "          $status$ = 2;\n" +
                                     "        }\n" +
                                     "        else if ($upgrade){\n" +
                                     "          $curr += \"old$requireVersion\";\n" +
                                     "          $status$ = 3;\n" +
                                     "        }\n" +
                                     "        else if ($requireOld){\n" +
                                     "          $curr += \"new$requireVersion\";\n" +
                                     "          $status$ = 4;\n" +
                                     "        }\n" +
                                     "        $curr += \";\";\n" +
                                     "\n" +
                                     "        arc.Core.settings.put(\"unc-checkFailed\", $curr);\n" +
                                     "        if (!arc.Core.settings.getBool(\"unc-warningShown\", false)){\n" +
                                     "          arc.Core.settings.put(\"unc-warningShown\", true);\n" +
                                     "\n" +
                                     "          arc.Events.on(mindustry.game.EventType.ClientLoadEvent.class, e -> {\n" +
                                     "            String $modStatus = arc.Core.settings.getString(\"unc-checkFailed\", \"\");\n" +
                                     "\n" +
                                     "            new arc.scene.ui.Dialog(){{\n" +
                                     "              setFillParent(true);\n" +
                                     "\n" +
                                     "              Runnable $rebuild = () -> {\n" +
                                     "                float w = Math.min(arc.Core.graphics.getWidth()/ arc.scene.ui.layout.Scl.scl(1.2f), 560);\n" +
                                     "\n" +
                                     "                cont.clearChildren();\n" +
                                     "                cont.table(main -> {\n" +
                                     "                  main.add(arc.Core.bundle.get(\"warn.uncLoadFailed\"));\n" +
                                     "                  main.row();\n" +
                                     "                  main.image().color(mindustry.graphics.Pal.accent).growX().height(5).colspan(2).pad(0).padBottom(8).padTop(8).margin(0);\n" +
                                     "                  main.row();\n" +
                                     "                  main.table(t -> {\n" +
                                     "                    t.add(arc.Core.bundle.get(\"warn.caused\")).color(arc.graphics.Color.lightGray).padBottom(10);\n" +
                                     "                    t.row();\n" +
                                     "                    t.pane(table -> {\n" +
                                     "                      for (String $s : $modStatus.split(\";\")) {\n" +
                                     "                        if ($s.isEmpty()) continue;\n" +
                                     "                        final String[] $modStat = $s.split(\"::\");\n" +
                                     "\n" +
                                     "                        final arc.files.ZipFi $f = new arc.files.ZipFi(new arc.files.Fi($modStat[0]));\n" +
                                     "                        final arc.files.Fi manifest = $f.child(\"mod.json\").exists() ? $f.child(\"mod.json\") :\n" +
                                     "                            $f.child(\"mod.hjson\").exists() ? $f.child(\"mod.hjson\") :\n" +
                                     "                                $f.child(\"plugin.json\").exists() ? $f.child(\"plugin.json\") :\n" +
                                     "                                    $f.child(\"plugin.hjson\");\n" +
                                     "\n" +
                                     "                        final arc.util.serialization.Jval $info = arc.util.serialization.Jval.read(manifest.reader());\n" +
                                     "                        final String name = $info.getString(\"name\", \"\");\n" +
                                     "                        final String displayName = $info.getString(\"displayName\", \"\");\n" +
                                     "\n" +
                                     "                        final arc.files.Fi $icon = $f.child(\"icon.png\");\n" +
                                     "                        table.table(modInf -> {\n" +
                                     "                          modInf.defaults().left();\n" +
                                     "                          modInf.image().size(112).get().setDrawable($icon.exists() ? new arc.scene.style.TextureRegionDrawable(new arc.graphics.g2d.TextureRegion(new arc.graphics.Texture($icon))) : mindustry.gen.Tex.nomap);\n" +
                                     "                          modInf.left().table(text -> {\n" +
                                     "                            text.left().defaults().left();\n" +
                                     "                            text.add(\"[accent]\" + displayName);\n" +
                                     "                            text.row();\n" +
                                     "                            text.add(\"[gray]\" + name);\n" +
                                     "                            text.row();\n" +
                                     "                            text.add(\"[crimson]\" + (\n" +
                                     "                                $modStat[1].equals(\"dis\") ? arc.Core.bundle.get(\"warn.uncDisabled\") :\n" +
                                     "                                    $modStat[1].equals(\"none\") ? arc.Core.bundle.get(\"warn.uncNotFound\") :\n" +
                                     "                                        $modStat[1].startsWith(\"old\") ? arc.Core.bundle.format(\"warn.uncVersionOld\", $modStat[1].replace(\"old\", \"\")) :\n" +
                                     "                                            arc.Core.bundle.format(\"warn.uncVersionNewer\", $modStat[1].replace(\"new\", \"\"))\n" +
                                     "                            ));\n" +
                                     "                          }).padLeft(5).top().growX();\n" +
                                     "                        }).padBottom(4).padLeft(12).padRight(12).growX().fillY().left();\n" +
                                     "                        table.row();\n" +
                                     "                        table.image().color(arc.graphics.Color.gray).growX().height(6).colspan(2).pad(0).margin(0);\n" +
                                     "                        table.row();\n" +
                                     "                      }\n" +
                                     "                    }).grow().maxWidth(w);\n" +
                                     "                  }).grow().top();\n" +
                                     "                  main.row();\n" +
                                     "                  main.image().color(mindustry.graphics.Pal.accent).growX().height(6).colspan(2).pad(0).padBottom(12).margin(0).bottom();\n" +
                                     "                  main.row();\n" +
                                     "                  main.add(arc.Core.bundle.format(\"warn.currentUncVersion\", $libFile != null ? \"\" + $libVersion : arc.Core.bundle.get(\"warn.libNotFound\"))).padBottom(10).bottom();\n" +
                                     "                  main.row();\n" +
                                     "\n" +
                                     "                  final arc.struct.Seq<arc.scene.ui.Button> $buttons = new arc.struct.Seq<>();\n" +
                                     "\n" +
                                     "                  if ($disabled.get()) {\n" +
                                     "                    $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get(\"warn.enableLib\"), () -> {\n" +
                                     "                      arc.Core.settings.put(\"mod-universe-core-enabled\", true);\n" +
                                     "                      mindustry.Vars.ui.showInfoOnHidden(\"@mods.reloadexit\", () -> {\n" +
                                     "                        arc.util.Log.info(\"Exiting to reload mods.\");\n" +
                                     "                        arc.Core.app.exit();\n" +
                                     "                      });\n" +
                                     "                    }));\n" +
                                     "                  } else {\n" +
                                     "                    $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get(\"warn.download\"), () -> {\n" +
                                     "                      final java.io.InputStream[] $stream = new java.io.InputStream[1];\n" +
                                     "                      final float[] $downloadProgress = {0};\n" +
                                     "\n" +
                                     "                      final mindustry.ui.dialogs.BaseDialog[] $di = new mindustry.ui.dialogs.BaseDialog[]{null};\n" +
                                     "\n" +
                                     "                      arc.util.Http.get(\"https://api.github.com/repos/EB-wilson/UniverseCore/releases/latest\").timeout(900).error((e) -> {\n" +
                                     "                        mindustry.Vars.ui.showException(arc.Core.bundle.get(\"warn.downloadFailed\"), e);\n" +
                                     "                        arc.util.Log.err(e);\n" +
                                     "                        $di[0].hide();\n" +
                                     "                      }).submit((res) -> {\n" +
                                     "                        final arc.util.serialization.Jval $json = arc.util.serialization.Jval.read(res.getResultAsString());\n" +
                                     "                        final arc.util.serialization.Jval.JsonArray $assets = $json.get(\"assets\").asArray();\n" +
                                     "\n" +
                                     "                        final arc.util.serialization.Jval $asset = $assets.find(j -> j.getString(\"name\").endsWith(\".jar\"));\n" +
                                     "\n" +
                                     "                        if ($asset != null) {\n" +
                                     "                          final String $downloadUrl = $asset.getString(\"browser_download_url\");\n" +
                                     "\n" +
                                     "                          arc.util.Http.get($downloadUrl, result -> {\n" +
                                     "                            $stream[0] = result.getResultAsStream();\n" +
                                     "                            final arc.files.Fi $temp = mindustry.Vars.tmpDirectory.child(\"UniverseCore.jar\");\n" +
                                     "                            final arc.files.Fi $file = mindustry.Vars.modDirectory.child(\"UniverseCore.jar\");\n" +
                                     "                            final long $length = result.getContentLength();\n" +
                                     "                            final arc.func.Floatc $cons = $length <= 0 ? f -> {\n" +
                                     "                            } : p -> $downloadProgress[0] = p;\n" +
                                     "\n" +
                                     "                            arc.util.io.Streams.copyProgress($stream[0], $temp.write(false), $length, 4096, $cons);\n" +
                                     "                            if ($libFile != null && $libFile.exists()) $libFile.delete();\n" +
                                     "                            $temp.moveTo($file);\n" +
                                     "                            try {\n" +
                                     "                              mindustry.Vars.mods.importMod($file);\n" +
                                     "                              $file.file().delete();\n" +
                                     "                              hide();\n" +
                                     "                              mindustry.Vars.ui.mods.show();\n" +
                                     "                            } catch (java.io.IOException e) {\n" +
                                     "                              mindustry.Vars.ui.showException(e);\n" +
                                     "                              arc.util.Log.err(e);\n" +
                                     "                              $di[0].hide();\n" +
                                     "                            }\n" +
                                     "                          }, e -> {\n" +
                                     "                            mindustry.Vars.ui.showException(arc.Core.bundle.get(\"warn.downloadFailed\"), e);\n" +
                                     "                            arc.util.Log.err(e);\n" +
                                     "                            $di[0].hide();\n" +
                                     "                          });\n" +
                                     "                        } else throw new RuntimeException(\"release file was not found\");\n" +
                                     "                      });\n" +
                                     "                      $di[0] = new mindustry.ui.dialogs.BaseDialog(\"\") {{\n" +
                                     "                        titleTable.clearChildren();\n" +
                                     "                        cont.table(mindustry.gen.Tex.pane, (t) -> {\n" +
                                     "                          t.add(arc.Core.bundle.get(\"warn.downloading\")).top().padTop(10).get();\n" +
                                     "                          t.row();\n" +
                                     "                          t.add(new mindustry.ui.Bar(() -> arc.util.Strings.autoFixed($downloadProgress[0], 1) + \"%\", () -> mindustry.graphics.Pal.accent, () -> $downloadProgress[0])).growX().height(30).pad(4);\n" +
                                     "                        }).size(320, 175);\n" +
                                     "                        cont.row();\n" +
                                     "                        cont.button(arc.Core.bundle.get(\"warn.cancel\"), () -> {\n" +
                                     "                          hide();\n" +
                                     "                          try {\n" +
                                     "                            if ($stream[0] != null) $stream[0].close();\n" +
                                     "                          } catch (java.io.IOException e) {\n" +
                                     "                            arc.util.Log.err(e);\n" +
                                     "                          }\n" +
                                     "                        }).fill();\n" +
                                     "                      }};\n" +
                                     "                      $di[0].show();\n" +
                                     "                    }));\n" +
                                     "                    $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get(\"warn.openfile\"), () -> {\n" +
                                     "                      mindustry.Vars.platform.showMultiFileChooser(fi -> {\n" +
                                     "                        final arc.files.ZipFi $file = new arc.files.ZipFi(fi);\n" +
                                     "                        final arc.files.Fi manifest = $file.child(\"mod.hjson\").exists() ? $file.child(\"mod.hjson\") : null;\n" +
                                     "\n" +
                                     "                        if (manifest == null) {\n" +
                                     "                          mindustry.Vars.ui.showErrorMessage(\"not a mod file, no mod.hjson found\");\n" +
                                     "                          return;\n" +
                                     "                        }\n" +
                                     "\n" +
                                     "                        final arc.util.serialization.Jval $info = arc.util.serialization.Jval.read(manifest.reader());\n" +
                                     "\n" +
                                     "                        if (!$info.getString(\"name\", \"\").equals(\"universe-core\")) {\n" +
                                     "                          mindustry.Vars.ui.showErrorMessage(\"not UniverseCore mod file\");\n" +
                                     "                        } else if ($versionValid.get($info.getString(\"version\", \"0.0.0\")) == 1) {\n" +
                                     "                          mindustry.Vars.ui.showErrorMessage(\"version was deprecated, require: $requireVersion, select: \" + $info.getString(\"version\", \"0.0.0\"));\n" +
                                     "                        } else if ($versionValid.get($info.getString(\"version\", \"0.0.0\")) == 2) {\n" +
                                     "                          mindustry.Vars.ui.showErrorMessage(\"version was too newer, require: $requireVersion, select: \" + $info.getString(\"version\", \"0.0.0\"));\n" +
                                     "                        } else {\n" +
                                     "                          try {\n" +
                                     "                            if ($libFile != null && $libFile.exists()) $libFile.delete();\n" +
                                     "                            mindustry.Vars.mods.importMod($file);\n" +
                                     "                            hide();\n" +
                                     "                            mindustry.Vars.ui.mods.show();\n" +
                                     "                          } catch (java.io.IOException e) {\n" +
                                     "                            mindustry.Vars.ui.showException(e);\n" +
                                     "                            arc.util.Log.err(e);\n" +
                                     "                          }\n" +
                                     "                        }\n" +
                                     "                      }, \"zip\", \"jar\");\n" +
                                     "                    }));\n" +
                                     "                  }\n" +
                                     "                  $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get(\"warn.goLibPage\"), () -> {\n" +
                                     "                    if (!arc.Core.app.openURI(\"https://github.com/EB-wilson/UniverseCore\")) {\n" +
                                     "                      mindustry.Vars.ui.showErrorMessage(\"@linkfail\");\n" +
                                     "                      arc.Core.app.setClipboardText(\"https://github.com/EB-wilson/UniverseCore\");\n" +
                                     "                    }\n" +
                                     "                  }));\n" +
                                     "                  $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get(\"warn.openModDir\"), () -> {\n" +
                                     "                    if (!arc.Core.app.openFolder(mindustry.Vars.modDirectory.path())) {\n" +
                                     "                      mindustry.Vars.ui.showInfo(arc.Core.bundle.get(\"warn.androidOpenFolder\"));\n" +
                                     "                      arc.Core.app.setClipboardText(mindustry.Vars.modDirectory.path());\n" +
                                     "                    }\n" +
                                     "                  }));\n" +
                                     "                  $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get(\"warn.exit\"), () -> arc.Core.app.exit()));\n" +
                                     "\n" +
                                     "                  main.table(buttons -> {\n" +
                                     "                    buttons.clearChildren();\n" +
                                     "                    if (arc.Core.scene.getWidth() < 168 * ($disabled.get() ? 4 : 5)) {\n" +
                                     "                      buttons.table(but -> {\n" +
                                     "                        but.defaults().growX().height(55).pad(4);\n" +
                                     "                        for (arc.scene.ui.Button button : $buttons) {\n" +
                                     "                          but.add(button);\n" +
                                     "                          but.row();\n" +
                                     "                        }\n" +
                                     "                      }).growX().fillY();\n" +
                                     "                    } else {\n" +
                                     "                      buttons.table(but -> {\n" +
                                     "                        but.defaults().width(160).height(55).pad(4);\n" +
                                     "                        for (arc.scene.ui.Button button : $buttons) {\n" +
                                     "                          but.add(button);\n" +
                                     "                        }\n" +
                                     "                      }).fill().bottom().padBottom(8);\n" +
                                     "                    }\n" +
                                     "                  }).growX().fillY();\n" +
                                     "                }).grow().top().pad(0).margin(0);\n" +
                                     "              };\n" +
                                     "\n" +
                                     "              $rebuild.run();\n" +
                                     "              resized($rebuild);\n" +
                                     "            }}.show();\n" +
                                     "          });\n" +
                                     "        }\n" +
                                     "      }\n" +
                                     "      else{\n" +
                                     "        arc.util.Log.info(\"dependence mod was not loaded, load it now\");\n" +
                                     "        arc.util.Log.info(\"you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\\ndon't worry, this is expected, it will not affect your game\");\n" +
                                     "        try{\n" +
                                     "          java.lang.reflect.Method $load = mindustry.mod.Mods.class.getDeclaredMethod(\"loadMod\", arc.files.Fi.class);\n" +
                                     "          $load.setAccessible(true);\n" +
                                     "          java.lang.reflect.Field $f = mindustry.mod.Mods.class.getDeclaredField(\"mods\");\n" +
                                     "          $f.setAccessible(true);\n" +
                                     "          arc.struct.Seq<mindustry.mod.Mods.LoadedMod> mods = (arc.struct.Seq<mindustry.mod.Mods.LoadedMod>) $f.get(mindustry.Vars.mods);\n" +
                                     "          mods.add((mindustry.mod.Mods.LoadedMod) $load.invoke(mindustry.Vars.mods, $libFile));\n" +
                                     "        }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException |\n" +
                                     "               java.lang.reflect.InvocationTargetException e){\n" +
                                     "          e.printStackTrace();\n" +
                                     "        }\n" +
                                     "      }\n" +
                                     "    }\n" +
                                     "  }\n" +
                                     "\n" +
                                     "  if($status$ == 0){\n" +
                                     "    universecore.UncCore.signup($className.class);\n" +
                                     "    $cinitField$\n" +
                                     "  }\n" +
                                     "  else{\n" +
                                     "    $cinitFieldError$\n" +
                                     "\n" +
                                     "    if($status$ == 1){\n" +
                                     "      arc.util.Log.err(\"universeCore mod was disabled\");\n" +
                                     "    }\n" +
                                     "    else if($status$ == 2){\n" +
                                     "      arc.util.Log.err(\"universeCore mod file was not found\");\n" +
                                     "    }\n" +
                                     "    else if($status$ == 3){\n" +
                                     "      arc.util.Log.err(\"universeCore version was deprecated, version: \" + $libVersionValue + \" require: $requireVersion\");\n" +
                                     "    }\n" +
                                     "    else if($status$ == 4){\n" +
                                     "      arc.util.Log.err(\"universeCore version was too newer, version: \" + $libVersionValue + \" require: $requireVersion\");\n" +
                                     "    }\n" +
                                     "  }\n" +
                                     "}\n";

  private static final HashMap<String , String> bundles = new HashMap<>();

  static {
    bundles.put("", "warn.uncLoadFailed = UniverseCore failed to load\n" +
                    "warn.uncDisabled = UniverseCore mod has been disabled\n" +
                    "warn.uncNotFound = UniverseCore mod file does not exist or is missing\n" +
                    "warn.libNotFound = NotFound\n" +
                    "warn.currentUncVersion = Current UniverseCore version: {0} It is recommended to install or update the latest version of UniverseCore\n" +
                    "warn.uncVersionOld = UniverseCore version is outdated, requires: {0}\n" +
                    "warn.uncVersionNewer = UniverseCore version is too newer, requires: {0}\n" +
                    "warn.download = Download\n" +
                    "warn.downloading = downloading...\n" +
                    "warn.downloadFailed = download failed\n" +
                    "warn.cancel = cancel\n" +
                    "warn.openfile = import from file\n" +
                    "warn.goLibPage = go to github\n" +
                    "warn.openModDir = Go to the mods directory\n" +
                    "warn.exit = quit\n" +
                    "warn.androidOpenFolder = Failed to open the directory, you can go to the following path:\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]The address has been copied to the clipboard\n" +
                    "warn.enableLib = enable mod\n" +
                    "warn.caused = The following mods cannot be loaded correctly due to abnormal Universe Core status\n");

    bundles.put("zh_CN", "warn.uncLoadFailed = UniverseCore 加载失败\n" +
                         "warn.uncDisabled = UniverseCore mod 已被禁用\n" +
                         "warn.uncNotFound = UniverseCore mod 文件不存在或已丢失\n" +
                         "warn.libNotFound = 未找到\n" +
                         "warn.currentUncVersion = 当前UniverseCore版本：{0}  建议安装或更新最新版本的UniverseCore\n" +
                         "warn.uncVersionOld = UniverseCore 版本过旧，需要：{0}\n" +
                         "warn.uncVersionNewer = UniverseCore 版本太过超前，当前需要：{0}\n" +
                         "warn.download = 下载\n" +
                         "warn.downloading = 下载中...\n" +
                         "warn.downloadFailed = 下载失败\n" +
                         "warn.cancel = 取消\n" +
                         "warn.openfile = 从文件导入\n" +
                         "warn.goLibPage = 前往github\n" +
                         "warn.openModDir = 前往mods目录\n" +
                         "warn.exit = 退出\n" +
                         "warn.androidOpenFolder = 打开目录失败，您可前往如下路径：\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]地址已复制到剪贴板\n" +
                         "warn.enableLib = 启用mod\n" +
                         "warn.caused = 以下mod由于UniverseCore状态异常无法正确加载\n");

    bundles.put("zh_TW", "warn.uncLoadFailed = UniverseCore 加載失敗\n" +
                         "warn.uncDisabled = UniverseCore mod 已被禁用\n" +
                         "warn.uncNotFound = UniverseCore mod 文件不存在或已丟失\n" +
                         "warn.libNotFound = 未找到\n" +
                         "warn.currentUncVersion = 當前UniverseCore版本：{0} 建議安裝或更新最新版本的UniverseCore\n" +
                         "warn.uncVersionOld = UniverseCore 版本過舊，需要：{0}\n" +
                         "warn.uncVersionNewer = UniverseCore 版本太過超前，當前需要：{0}\n" +
                         "warn.download = 下載\n" +
                         "warn.downloading = 下載中...\n" +
                         "warn.downloadFailed = 下載失敗\n" +
                         "warn.cancel = 取消\n" +
                         "warn.openfile = 從文件導入\n" +
                         "warn.goLibPage = 前往github\n" +
                         "warn.openModDir = 前往mods目錄\n" +
                         "warn.exit = 退出\n" +
                         "warn.androidOpenFolder = 打開目錄失敗，您可前往如下路徑：\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]地址已復製到剪貼板\n" +
                         "warn.enableLib = 啓用mod\n" +
                         "warn.caused = 以下mod由於Universe Core狀態異常無法正確加載\n");

    bundles.put("ru", "warn.uncLoadFailed = UniverseCore не удалось загрузить\n" +
                      "warn.uncDisabled = Мод UniverseCore отключен.\n" +
                      "warn.uncNotFound = Файл мода UniverseCore не существует или отсутствует\n" +
                      "warn.libNotFound = Не найден\n" +
                      "warn.currentUncVersion = Текущая версия UniverseCore: {0} Рекомендуется установить или обновить последнюю версию UniverseCore.\n" +
                      "warn.uncVersionOld = Версия UniverseCore устарела, требуется: {0}\n" +
                      "warn.uncVersionNewer = Слишком новая версия UniverseCore, в настоящее время требуется: {0}\n" +
                      "warn.download = скачать\n" +
                      "warn.downloading = скачивание...\n" +
                      "warn.downloadFailed = Загрузка не удалась\n" +
                      "warn.cancel = Отмена\n" +
                      "warn.openfile = импортировать из файла\n" +
                      "warn.goLibPage = перейти на гитхаб\n" +
                      "warn.openModDir = Перейдите в каталог модов\n" +
                      "warn.exit = покидать\n" +
                      "warn.androidOpenFolder = Не удалось открыть каталог, можно перейти по следующему пути:\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]Адрес скопирован в буфер обмена\n" +
                      "warn.enableLib = включить моды\n" +
                      "warn.caused = Следующие моды не могут быть загружены правильно из-за ненормального статуса Universe Core.\n");

    bundles.put("ja", "warn.uncLoadFailed = UniverseCore を読み込めませんでした\n" +
                      "warn.uncDisabled = UniverseCore mod が無効化されました\n" +
                      "warn.uncNotFound = UniverseCore mod ファイルが存在しないか、見つかりません\n" +
                      "warn.libNotFound = 見つかりません\n" +
                      "warn.currentUncVersion = 現在の UniverseCore バージョン: {0} UniverseCore の最新バージョンをインストールまたは更新することをお勧めします\n" +
                      "warn.uncVersionOld = UniverseCore のバージョンが古くなっています。必要なもの: {0}\n" +
                      "warn.uncVersionNewer = UniverseCore のバージョンが新しすぎます。現在必要なもの: {0}\n" +
                      "warn.download = ダウンロード\n" +
                      "warn.downloading = ダウンロード中...\n" +
                      "warn.downloadFailed = ダウンロードに失敗しました\n" +
                      "warn.cancel = キャンセル\n" +
                      "warn.openfile = ファイルからインポート\n" +
                      "warn.goLibPage = ギットハブに行く\n" +
                      "warn.openModDir = mods ディレクトリに移動します。\n" +
                      "warn.exit = 終了する\n" +
                      "warn.androidOpenFolder = ディレクトリを開けませんでした。次のパスに移動できます:\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]アドレスはクリップボードにコピーされました\n" +
                      "warn.enableLib = 改造を有効にする\n" +
                      "warn.caused = ユニバースコアの状態異常により、以下のMODが正常にロードできない\n");
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
    for(TypeElement anno : annotations){
      for(Element element : roundEnv.getElementsAnnotatedWith(anno)){
        Annotations.ImportUNC annotation = element.getAnnotation(Annotations.ImportUNC.class);

        String[] s = annotation.requireVersion().split("\\.");
        boolean c = s.length == 3;
        if(c){
          try {
            for (String n : s) {
              Integer.parseInt(n);
            }
          }catch (Throwable ignored){
            c = false;
          }
        }
        if (!c) throw new IllegalArgumentException("illegal version format: " + annotation.requireVersion() + ", correct format: \"x.y.z\"");

        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) trees.getTree(element);
        if(!tree.sym.getSuperclass().asElement().getQualifiedName().toString().equals("mindustry.mod.Mod"))
           throw new IllegalArgumentException("import universe core require the class extend mindustry.mod.Mod");

        ArrayList<JCTree.JCExpressionStatement> init = new ArrayList<>();
        ArrayList<JCTree> vars = new ArrayList<>();

        Symbol.VarSymbol status = new Symbol.VarSymbol(
            Modifier.PRIVATE | Modifier.STATIC,
            names.fromString(STATUS_FIELD),
            symtab.byteType,
            tree.sym
        );
        tree.defs = tree.defs.prepend(maker.VarDef(status, null));

        for(JCTree def: tree.defs){
          if(def instanceof JCTree.JCVariableDecl){
            JCTree.JCVariableDecl variable = (JCTree.JCVariableDecl) def;
            if((variable.mods.flags & Modifier.STATIC) != 0){
              if(variable.init != null){
                init.add(
                    maker.Exec(
                        maker.Assign(
                            maker.Ident(variable), variable.init)));
                variable.init = null;
              }
              vars.add(variable);
            }
          }
        }

        ArrayList<JCTree> tmp = new ArrayList<>(Arrays.asList(tree.defs.toArray(new JCTree[0])));
        tmp.removeIf(vars::contains);
        tree.defs = List.from(tmp);
        
        String genCode = genLoadCode(tree.sym.getQualifiedName().toString(), annotation.requireVersion(), List.from(init));

        JCTree.JCBlock 
            preLoadBody = parsers.newParser(genCode, false, false, false).block(),
            cinit = null;

        for(JCTree def: tree.defs){
          if(def instanceof JCTree.JCBlock){
            if(((JCTree.JCBlock) def).isStatic()){
              cinit = ((JCTree.JCBlock) def);
            }
          }
          if(def instanceof JCTree.JCMethodDecl){
            JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) def;
            if(method.sym.isConstructor() && method.params.size() == 0){
              JCTree.JCClassDecl internalClass = maker.ClassDef(
                  maker.Modifiers(Modifier.PRIVATE),
                  names.fromString("INIT_INTERNAL"),
                  List.nil(),
                  null,
                  List.nil(),
                  List.of(maker.MethodDef(
                      maker.Modifiers(Modifier.PUBLIC),
                      names.init,
                      maker.TypeIdent(TypeTag.VOID),
                      List.nil(),
                      List.nil(),
                      List.nil(),
                      maker.Block(0, List.from(method.body.stats.toArray(new JCTree.JCStatement[0]))),
                      null
                  ))
              );
              tree.defs = tree.defs.append(internalClass);
              method.body = parsers.newParser("{if(" + STATUS_FIELD + " != 0) return; new INIT_INTERNAL();}", false, false, false).block();
            }
            else if(!method.sym.isConstructor()){
              method.body.stats = method.body.stats.prepend(
                  parsers.newParser("if(" + STATUS_FIELD + " != 0) return " + getDef(method.restype.type.getKind()) + ";", false, false, false).parseStatement()
              );
            }
          }
        }
        
        if(cinit == null){
          tree.defs = tree.defs.prepend(maker.Block(Flags.STATIC, preLoadBody.stats));
        }
        else{
          cinit.stats = cinit.stats.prependList(
              preLoadBody.stats
          );
        }

        tree.defs = tree.defs.prependList(List.from(vars));

        genLog(anno, tree);
      }
    }

    return super.process(annotations, roundEnv);
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes(){
    HashSet<String> annotations = new HashSet<>();
    annotations.add(Annotations.ImportUNC.class.getCanonicalName());
    return annotations;
  }
  
  private String genLoadCode(String modMain, String requireVersion, List<JCTree.JCExpressionStatement> initList){
    StringBuilder bundles = new StringBuilder();
    boolean first = true;
    for(Map.Entry<String, String> entry : ImportUNCProcessor.bundles.entrySet()){
      bundles.append(first ? "" : ", ").append("\"").append(entry.getKey()).append("\", \"").append(format(entry.getValue())).append("\"");
      first = false;
    }

    StringBuilder init = new StringBuilder();
    StringBuilder errorInit = new StringBuilder();

    for(JCTree.JCExpressionStatement state: initList){
      init.append(state);
      errorInit.append(((JCTree.JCAssign)state.expr).getVariable())
          .append(" = ")
          .append(getDef(((JCTree.JCAssign)state.expr).getVariable().type.getKind()))
          .append(";")
          .append(System.lineSeparator());
    }

    return code.replace("$bundles", bundles.toString())
        .replace("$requireVersion", requireVersion)
        .replace("$className", modMain)
        .replace("$cinitField$", init.toString())
        .replace("$cinitFieldError$", errorInit.toString());
  }

  private static String format(String input) {
    BufferedReader reader = new BufferedReader(new StringReader(input));
    StringBuilder res = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        res.append(line).append("\\n");
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return res.toString();
  }

  private static String getDef(TypeKind kind){
    switch(kind){
      case VOID: return "";
      case INT:
      case SHORT:
      case BYTE:
      case LONG:
      case FLOAT:
      case DOUBLE:
      case CHAR: return  "0";
      case BOOLEAN: return  "false";
      default: return  "null";
    }
  }
}
