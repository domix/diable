package com.domingosuarez.diable.ast;

import com.domingosuarez.diable.ProviderFactory;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;
import static org.codehaus.groovy.control.CompilePhase.SEMANTIC_ANALYSIS;

/**
 * Created by domix on 18/05/15.
 */
@GroovyASTTransformation(phase = SEMANTIC_ANALYSIS)
public class DIableASTTransformation extends AbstractASTTransformation {
  private static final ClassNode SOFT_REF = makeWithoutCaching(SoftReference.class, false);
  private static final Expression NULL_EXPR = ConstantExpression.NULL;
  private static final List<String> INVALID_FIELDS = Arrays.asList("$staticClassInfo", "__$stMC", "metaClass");

  @Override
  public void visit(ASTNode[] nodes, SourceUnit source) {
    init(nodes, source);
    AnnotatedNode parent = (AnnotatedNode) nodes[1];
    AnnotationNode node = (AnnotationNode) nodes[0];

    if (parent instanceof ClassNode) {
      List<FieldNode> fields = ((ClassNode) parent).getFields();
      fields.stream().forEach(field -> {
        if (field.getAnnotations().size() > 0) {
          visitField(node, field);
        }
      });
    }
  }

  static void visitField(AnnotationNode node, FieldNode fieldNode) {
    final Expression soft = node.getMember("soft");
    final Expression init = getInitExpr(fieldNode);

    //fieldNode.rename("$" + fieldNode.getName());
    //fieldNode.setModifiers(ACC_PRIVATE | (fieldNode.getModifiers() & (~(ACC_PUBLIC | ACC_PROTECTED))));

    if (soft instanceof ConstantExpression && ((ConstantExpression) soft).getValue().equals(true)) {
      createSoft(fieldNode, init);
    } else {
      create(fieldNode, init);
      // @DIable not meaningful with primitive so convert to wrapper if needed
      if (ClassHelper.isPrimitiveType(fieldNode.getType())) {
        fieldNode.setType(ClassHelper.getWrapper(fieldNode.getType()));
      }
    }
  }

  private static void create(FieldNode fieldNode, final Expression initExpr) {
    final BlockStatement body = new BlockStatement();
    if (fieldNode.isStatic()) {
      addHolderClassIdiomBody(body, fieldNode, initExpr);
    } else if (fieldNode.isVolatile()) {
      addDoubleCheckedLockingBody(body, fieldNode, initExpr);
    } else {
      addNonThreadSafeBody(body, fieldNode, initExpr);
    }
    addMethod(fieldNode, body, fieldNode.getType());
  }

  private static void addHolderClassIdiomBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
    final ClassNode declaringClass = fieldNode.getDeclaringClass();
    final ClassNode fieldType = fieldNode.getType();
    final int visibility = ACC_PRIVATE | ACC_STATIC;
    final String fullName = declaringClass.getName() + "$" + fieldType.getNameWithoutPackage() + "Holder_" + fieldNode.getName().substring(1);
    final InnerClassNode holderClass = new InnerClassNode(declaringClass, fullName, visibility, ClassHelper.OBJECT_TYPE);
    final String innerFieldName = "INSTANCE";
    holderClass.addField(innerFieldName, ACC_PRIVATE | ACC_STATIC | ACC_FINAL, fieldType, initExpr);
    final Expression innerField = propX(classX(holderClass), innerFieldName);
    declaringClass.getModule().addClass(holderClass);
    body.addStatement(returnS(innerField));
  }

  private static void addDoubleCheckedLockingBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
    final Expression fieldExpr = varX(fieldNode);
    final VariableExpression localVar = varX(fieldNode.getName() + "_local");
    body.addStatement(declS(localVar, fieldExpr));
    body.addStatement(ifElseS(
      notNullX(localVar),
      returnS(localVar),
      new SynchronizedStatement(
        syncTarget(fieldNode),
        ifElseS(
          notNullX(fieldExpr),
          returnS(fieldExpr),
          returnS(assignX(fieldExpr, initExpr))
        )
      )
    ));
  }

  private static void addNonThreadSafeBody(BlockStatement body, FieldNode fieldNode, Expression initExpr) {
    final Expression fieldExpr = varX(fieldNode);
    body.addStatement(ifElseS(notNullX(fieldExpr), stmt(fieldExpr), assignS(fieldExpr, initExpr)));
  }

  private static void addMethod(FieldNode fieldNode, BlockStatement body, ClassNode type) {
    int visibility = ACC_PUBLIC;
    if (fieldNode.isStatic()) visibility |= ACC_STATIC;
    final String name = "get" + MetaClassHelper.capitalize(fieldNode.getName().substring(1));
    fieldNode.getDeclaringClass().addMethod(name, visibility, type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);

    createSetter(fieldNode, type);
  }

  private static void createSetter(FieldNode fieldNode, ClassNode type) {
    final BlockStatement body = new BlockStatement();
    final Expression fieldExpr = varX(fieldNode);
    final String name = "set" + MetaClassHelper.capitalize(fieldNode.getName().substring(1));
    final Parameter parameter = param(type, "value");
    final Expression paramExpr = varX(parameter);
    body.addStatement(assignS(fieldExpr, paramExpr));
    int visibility = ACC_PUBLIC;
    if (fieldNode.isStatic()) visibility |= ACC_STATIC;
    fieldNode.getDeclaringClass().addMethod(name, visibility, ClassHelper.VOID_TYPE, params(parameter), ClassNode.EMPTY_ARRAY, body);
  }


  private static void createSoft(FieldNode fieldNode, Expression initExpr) {
    final ClassNode type = fieldNode.getType();
    fieldNode.setType(SOFT_REF);
    createSoftGetter(fieldNode, initExpr, type);
    createSoftSetter(fieldNode, type);
  }

  private static void createSoftGetter(FieldNode fieldNode, Expression initExpr, ClassNode type) {
    final BlockStatement body = new BlockStatement();
    final Expression fieldExpr = varX(fieldNode);
    final Expression resExpr = varX("res", type);
    final MethodCallExpression callExpression = callX(fieldExpr, "get");
    callExpression.setSafe(true);
    body.addStatement(declS(resExpr, callExpression));

    final Statement mainIf = ifElseS(notNullX(resExpr), stmt(resExpr), block(
      assignS(resExpr, initExpr),
      assignS(fieldExpr, ctorX(SOFT_REF, resExpr)),
      stmt(resExpr)));

    if (fieldNode.isVolatile()) {
      body.addStatement(ifElseS(
        notNullX(resExpr),
        stmt(resExpr),
        new SynchronizedStatement(syncTarget(fieldNode), block(
          assignS(resExpr, callExpression),
          mainIf)
        )
      ));
    } else {
      body.addStatement(mainIf);
    }
    addMethod(fieldNode, body, type);
  }

  private static void createSoftSetter(FieldNode fieldNode, ClassNode type) {
    final BlockStatement body = new BlockStatement();
    final Expression fieldExpr = varX(fieldNode);
    final String name = "set" + MetaClassHelper.capitalize(fieldNode.getName().substring(1));
    final Parameter parameter = param(type, "value");
    final Expression paramExpr = varX(parameter);
    body.addStatement(ifElseS(
      notNullX(paramExpr),
      assignS(fieldExpr, ctorX(SOFT_REF, paramExpr)),
      assignS(fieldExpr, NULL_EXPR)
    ));
    int visibility = ACC_PUBLIC;
    if (fieldNode.isStatic()) visibility |= ACC_STATIC;
    fieldNode.getDeclaringClass().addMethod(name, visibility, ClassHelper.VOID_TYPE, params(parameter), ClassNode.EMPTY_ARRAY, body);
  }

  private static Expression syncTarget(FieldNode fieldNode) {
    return fieldNode.isStatic() ? classX(fieldNode.getDeclaringClass()) : varX("this");
  }

  private static Expression getInitExpr(FieldNode fieldNode) {
    Expression initExpr = fieldNode.getInitialValueExpression();
    fieldNode.setInitialValueExpression(null);

    boolean isInvalidField = INVALID_FIELDS.stream().anyMatch(str -> str.equals(fieldNode.getName()));

    if (initExpr == null && !isInvalidField) {

      FieldExpression fieldExpression = new FieldExpression(fieldNode);
      fieldExpression.setUseReferenceDirectly(true);

      StaticMethodCallExpression findValue = new StaticMethodCallExpression(ClassHelper.make(ProviderFactory.class, false), "findValue", new ArgumentListExpression(
        constX(fieldNode.getName()),
        new ClassExpression(fieldNode.getOwner())
      ));

      System.out.println("Generated code");
      System.out.println(findValue.getText());

      initExpr = findValue;
    }

    return initExpr;
  }
}
