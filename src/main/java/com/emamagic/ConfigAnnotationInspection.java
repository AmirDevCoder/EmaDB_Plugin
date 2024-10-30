package com.emamagic;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.ResourceBundle;

public class ConfigAnnotationInspection extends LocalInspectionTool {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("messages.InspectionBundle");
    private static final String MESSAGE = bundle.getString("inspection.configAnnotation.description");

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                if (element instanceof PsiClass) {
                    visitClass((PsiClass) element);
                }
            }

            public void visitClass(@NotNull PsiClass aClass) {
                if (aClass.hasAnnotation("com.emamagic.annotation.Config")) {
                    boolean implementsMyInterface = false;
                    for (PsiClass anInterface : aClass.getInterfaces()) {
                        if ("com.emamagic.conf.EmaConfig".equals(anInterface.getQualifiedName())) {
                            implementsMyInterface = true;
                            break;
                        }
                    }
                    if (!implementsMyInterface) {
                        holder.registerProblem(
                                Objects.requireNonNull(aClass.getNameIdentifier()),
                                MESSAGE,
                                ProblemHighlightType.GENERIC_ERROR,
                                new ImplementEmaConfigQuickFix()
                        );
                    }
                }
            }
        };
    }

    private static class ImplementEmaConfigQuickFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getFamilyName() {
            return "Implement EmaConfig interface";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiClass psiClass = (PsiClass) descriptor.getPsiElement().getParent();
            if (psiClass != null) {
                PsiElementFactory factory = PsiElementFactory.getInstance(project);
                PsiReferenceList referenceList = psiClass.getImplementsList();
                if (referenceList != null) {
                    referenceList.add(factory.createReferenceElementByFQClassName("com.emamagic.conf.EmaConfig", psiClass.getResolveScope()));
                }
            }
        }
    }
}


