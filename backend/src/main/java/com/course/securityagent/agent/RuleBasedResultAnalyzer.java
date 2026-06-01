package com.course.securityagent.agent;

import com.course.securityagent.entity.TestResult;
import com.course.securityagent.entity.TestTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RuleBasedResultAnalyzer implements ResultAnalyzer {

    @Override
    public List<TestResult> analyze(TestTask task) {
        return switch (task.getTestType()) {
            case "未授权访问测试" -> List.of(
                    result(task, "后台接口未授权访问", "访问控制", "HIGH", "/api/admin/users",
                            "未登录状态下访问管理接口仍返回敏感数据，可能导致用户信息或配置数据泄露。",
                            "1. 清空登录态或使用匿名会话访问目标接口。\n2. 请求管理接口 /api/admin/users。\n3. 观察响应中是否包含用户列表、角色或内部配置字段。",
                            "GET /api/admin/users HTTP/1.1\nHost: demo.local\nAuthorization: <empty>",
                            "HTTP/1.1 200 OK\n{\"code\":200,\"data\":[{\"username\":\"admin\",\"role\":\"ADMIN\"}]}",
                            "后端统一增加鉴权拦截器，对管理接口执行登录态和角色权限校验。"),
                    result(task, "接口错误信息暴露访问路径", "信息泄露", "LOW", "/api/admin/config",
                            "未授权请求返回了内部接口路径和权限配置说明，可辅助攻击者枚举接口。",
                            "1. 匿名访问管理配置接口。\n2. 观察错误响应是否包含内部路径、类名或权限编码。",
                            "GET /api/admin/config HTTP/1.1\nHost: demo.local",
                            "HTTP/1.1 403 Forbidden\n{\"message\":\"permission admin:config:read required\"}",
                            "统一错误响应，避免直接暴露内部权限标识和实现细节。")
            );
            case "弱口令测试" -> List.of(
                    result(task, "系统存在弱口令账号", "身份认证", "MEDIUM", "/login",
                            "检测到账户使用常见弱密码组合，攻击者可能通过低频猜测获得系统权限。",
                            "1. 在授权演示账号范围内尝试常见弱密码。\n2. 观察是否存在 admin/123456、test/test123 等组合。\n3. 记录命中账号并停止进一步尝试。",
                            "POST /login HTTP/1.1\nContent-Type: application/json\n\n{\"username\":\"admin\",\"password\":\"123456\"}",
                            "HTTP/1.1 200 OK\n{\"token\":\"***\",\"role\":\"ADMIN\"}",
                            "增加密码复杂度校验、初始密码强制修改、登录失败锁定和验证码策略。"),
                    result(task, "登录失败提示可辅助账号枚举", "身份认证", "LOW", "/login",
                            "登录失败信息区分账号不存在和密码错误，可能帮助攻击者判断有效账号。",
                            "1. 分别输入不存在账号和存在账号错误密码。\n2. 对比失败提示是否存在差异。",
                            "POST /login HTTP/1.1\n\n{\"username\":\"not_exists\",\"password\":\"test\"}",
                            "HTTP/1.1 401 Unauthorized\n{\"message\":\"用户不存在\"}",
                            "统一登录失败提示为“用户名或密码错误”，降低账号枚举风险。")
            );
            case "越权访问测试" -> List.of(
                    result(task, "订单详情接口存在水平越权", "访问控制", "HIGH", "/api/orders/10086?userId=2",
                            "修改 userId/orderId 参数后可访问其他用户订单，说明服务端未根据登录态绑定资源归属。",
                            "1. 使用用户 A 登录并访问自己的订单详情。\n2. 修改 orderId 或 userId 为用户 B 的资源。\n3. 观察是否返回用户 B 的订单数据。",
                            "GET /api/orders/10086?userId=2 HTTP/1.1\nAuthorization: Bearer userA-token",
                            "HTTP/1.1 200 OK\n{\"orderId\":10086,\"ownerUserId\":2,\"amount\":399}",
                            "服务端根据登录态绑定用户身份，不信任前端传入的 userId，并校验资源归属。"),
                    result(task, "管理接口缺少角色权限校验", "访问控制", "MEDIUM", "/api/admin/config",
                            "普通登录用户可访问部分管理配置接口，存在垂直越权风险。",
                            "1. 使用普通用户登录。\n2. 访问管理配置接口。\n3. 观察是否返回配置详情或允许修改。",
                            "GET /api/admin/config HTTP/1.1\nAuthorization: Bearer user-token",
                            "HTTP/1.1 200 OK\n{\"systemMode\":\"debug\"}",
                            "建立 RBAC 权限模型，对管理接口增加角色和权限点校验。")
            );
            case "敏感信息泄露测试" -> List.of(
                    result(task, "接口响应泄露敏感字段", "信息泄露", "MEDIUM", "/api/profile",
                            "响应中包含手机号、邮箱、token、内部路径等敏感信息，超出前端展示所需范围。",
                            "1. 登录后访问用户资料接口。\n2. 检查响应字段是否包含 token、内部路径、完整手机号等敏感数据。",
                            "GET /api/profile HTTP/1.1\nAuthorization: Bearer demo-token",
                            "HTTP/1.1 200 OK\n{\"phone\":\"13812345678\",\"token\":\"debug-token\",\"path\":\"/opt/app/config\"}",
                            "返回数据脱敏，按最小必要原则控制字段，避免暴露内部调试信息。"),
                    result(task, "异常页面暴露技术栈信息", "信息泄露", "LOW", "/error",
                            "异常响应包含框架版本、堆栈片段或内部类名，可能辅助攻击者判断攻击面。",
                            "1. 构造格式错误的请求参数。\n2. 观察错误页面是否包含堆栈和框架版本。",
                            "GET /api/detail?id=%27 HTTP/1.1",
                            "HTTP/1.1 500 Internal Server Error\njava.lang.IllegalArgumentException at com.demo.Service",
                            "生产环境关闭详细错误输出，统一异常响应格式并记录到服务端日志。")
            );
            case "接口参数篡改测试" -> List.of(
                    result(task, "接口参数缺少服务端校验", "业务安全", "MEDIUM", "/api/payments",
                            "修改 price、role、status 等参数后服务端未校验，可能造成金额或权限异常。",
                            "1. 捕获正常业务请求。\n2. 修改 price、role、status 等关键参数。\n3. 观察服务端是否仍接受请求。",
                            "POST /api/payments HTTP/1.1\n\n{\"orderId\":7,\"price\":0.01,\"status\":\"PAID\"}",
                            "HTTP/1.1 200 OK\n{\"message\":\"payment accepted\",\"paidAmount\":0.01}",
                            "关键参数由服务端计算或校验，前端传参只能作为展示或查询条件。"),
                    result(task, "搜索接口缺少参数白名单", "输入校验", "LOW", "/api/search?debug=true",
                            "接口接收未声明参数并改变响应内容，可能引发非预期查询或调试信息暴露。",
                            "1. 在查询接口中追加未声明参数。\n2. 对比正常响应和篡改响应差异。",
                            "GET /api/search?keyword=test&debug=true HTTP/1.1",
                            "HTTP/1.1 200 OK\n{\"sql\":\"select * from demo where keyword=?\",\"data\":[]}",
                            "增加参数白名单、类型校验和默认值约束，拒绝非法参数。")
            );
            default -> List.of(result(task, "通用安全配置风险", "安全配置", "LOW", task.getTargetUrl(),
                    "当前测试类型未匹配专用规则，系统生成通用风险提示。",
                    "1. 确认测试类型。\n2. 执行通用基线检查。\n3. 输出后续人工确认建议。",
                    "GET " + task.getTargetUrl() + " HTTP/1.1",
                    "HTTP/1.1 200 OK\n{\"message\":\"demo\"}",
                    "补充更细粒度规则模板，并在授权范围内接入真实扫描器。"));
        };
    }

    private TestResult result(TestTask task, String name, String type, String level, String path, String description,
                              String steps, String request, String response, String suggestion) {
        TestResult result = new TestResult();
        result.setTaskId(task.getId());
        result.setVulnerabilityName(name);
        result.setVulnerabilityType(type);
        result.setRiskLevel(level);
        result.setUrl(path.startsWith("http") ? path : task.getTargetUrl() + path);
        result.setDescription(description);
        result.setReproduceSteps(steps);
        result.setRequestExample(request);
        result.setResponseExample(response);
        result.setSuggestion(suggestion);
        result.setStatus("OPEN");
        result.setCreatedAt(LocalDateTime.now());
        return result;
    }
}
