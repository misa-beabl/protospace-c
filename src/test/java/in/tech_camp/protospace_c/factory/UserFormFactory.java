// 用于快速生成测试实例

package in.tech_camp.protospace_c.factory;

import com.github.javafaker.Faker;

import in.tech_camp.protospace_c.form.UserForm;

public class UserFormFactory {
    private static final Faker faker = new Faker();

    public static UserForm createUser() {
        UserForm userForm = new UserForm();

        userForm.setEmail(faker.internet().emailAddress());

        // 保证生成用户名长度符合规定
        String generatedUsername = faker.name().username();
        if (generatedUsername.length() > 6) {
            generatedUsername = generatedUsername.substring(0, 6);
        }
        userForm.setNickname(generatedUsername);

        // 其他必填项自动生成
        userForm.setProfile(faker.lorem().sentence(10, 5)); // 10单词，5变体
        userForm.setAffiliation(faker.company().name());
        userForm.setPosition(faker.job().position());

        String password = faker.internet().password(6, 12);
        userForm.setPassword(password);
        userForm.setPasswordConfirmation(password);

        return userForm;
    }
}
