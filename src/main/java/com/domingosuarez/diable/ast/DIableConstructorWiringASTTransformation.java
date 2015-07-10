package com.domingosuarez.diable.ast;

import com.domingosuarez.diable.ProviderFactory;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS;

/**
 * Created by angelpimentel on 7/2/15.
 */
@GroovyASTTransformation(phase = SEMANTIC_ANALYSIS)
public class DIableConstructorWiringASTTransformation extends AbstractASTTransformation {

  static final Class MY_CLASS = DIableConstructorWiringASTTransformation.class;
  static final ClassNode MY_TYPE = make(MY_CLASS);
  static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

  public void visit(ASTNode[] nodes, SourceUnit source) {
    init(nodes, source);
    AnnotatedNode parent = (AnnotatedNode) nodes[1];
    AnnotationNode anno = (AnnotationNode) nodes[0];
    if (parent instanceof ClassNode) {
      ClassNode cNode = (ClassNode) parent;
      configureConstructor(cNode);
    }
  }

  public static void configureConstructor(ClassNode cNode) {
    int constructorsSize = cNode.getDeclaredConstructors().size();
    if (constructorsSize == 0) {
      cNode.addConstructor(createConstructor());
    } else {
      for (int i = 0; i < constructorsSize; i++) {
        updateConstructor(cNode.getDeclaredConstructors().get(i));
      }
    }
  }

  public static ConstructorNode createConstructor() {
    BlockStatement constructorBody = new BlockStatement();
    constructorBody.addStatement(assignWiredStatement());
    ConstructorNode constructorNode = new ConstructorNode(ACC_PUBLIC, constructorBody);
    return constructorNode;
  }

  public static void updateConstructor(ConstructorNode constructorNode) {
    BlockStatement methodBody = new BlockStatement();
    methodBody.addStatement(constructorNode.getCode());
    methodBody.addStatement(assignWiredStatement());
    constructorNode.setCode(methodBody);
  }

  private static Statement assignWiredStatement() {
    StaticMethodCallExpression getWiredInstance = new StaticMethodCallExpression(
      ClassHelper.make(ProviderFactory.class, false), "wire", new ArgumentListExpression(varX("this"))
    );
    return new ExpressionStatement(getWiredInstance);
  }
}