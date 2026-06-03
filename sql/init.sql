CREATE DATABASE IF NOT EXISTS security_agent_platform
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE security_agent_platform;

DROP TABLE IF EXISTS test_report;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS security_knowledge;
DROP TABLE IF EXISTS remediation_ticket;
DROP TABLE IF EXISTS agent_decision;
DROP TABLE IF EXISTS user_score;
DROP TABLE IF EXISTS user_achievement;
DROP TABLE IF EXISTS challenge_submit;
DROP TABLE IF EXISTS challenge;
DROP TABLE IF EXISTS test_log;
DROP TABLE IF EXISTS test_result;
DROP TABLE IF EXISTS test_task;
DROP TABLE IF EXISTS system_config;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(128) NOT NULL COMMENT 'SHA-256 密码哈希',
  nickname VARCHAR(64) NOT NULL COMMENT '昵称',
  email VARCHAR(128) COMMENT '邮箱',
  phone VARCHAR(32) COMMENT '手机号',
  role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户表';

CREATE TABLE system_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
  config_key VARCHAR(64) NOT NULL UNIQUE COMMENT '配置键',
  config_value TEXT COMMENT '配置值',
  config_label VARCHAR(128) COMMENT '配置名称',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_config_key (config_key)
) COMMENT='系统配置表';

CREATE TABLE test_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
  task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
  target_url VARCHAR(512) NOT NULL COMMENT '目标地址',
  test_type VARCHAR(64) NOT NULL COMMENT '测试类型',
  description TEXT COMMENT '任务描述',
  plan TEXT COMMENT 'Agent生成的测试计划',
  test_depth VARCHAR(32) NOT NULL DEFAULT '标准' COMMENT '测试深度：快速/标准/深入',
  generate_report TINYINT NOT NULL DEFAULT 1 COMMENT '是否生成报告',
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
  create_user_id BIGINT COMMENT '创建用户ID',
  create_username VARCHAR(64) COMMENT '创建用户名',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_task_status (status),
  INDEX idx_task_type (test_type),
  INDEX idx_task_created_at (created_at)
) COMMENT='测试任务表';

CREATE TABLE test_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  vulnerability_name VARCHAR(128) NOT NULL COMMENT '漏洞名称',
  vulnerability_type VARCHAR(64) COMMENT '漏洞类型',
  risk_level VARCHAR(32) NOT NULL COMMENT '风险等级：HIGH/MEDIUM/LOW',
  url VARCHAR(512) COMMENT '影响地址',
  description TEXT COMMENT '漏洞描述',
  reproduce_steps TEXT COMMENT '复现步骤',
  request_example TEXT COMMENT '请求示例',
  response_example TEXT COMMENT '响应示例',
  suggestion TEXT COMMENT '修复建议',
  status VARCHAR(32) NOT NULL DEFAULT 'OPEN' COMMENT '处理状态',
  risk_score DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '风险评分',
  impact_score DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '影响分',
  exploitability_score DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '可利用性分',
  difficulty_score DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '利用难度分',
  risk_vector VARCHAR(255) COMMENT '风险向量',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_result_task_id (task_id),
  INDEX idx_result_risk_level (risk_level),
  INDEX idx_result_type (vulnerability_type)
) COMMENT='测试结果表';

CREATE TABLE agent_decision (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '决策ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  step_no INT NOT NULL COMMENT '步骤序号',
  stage VARCHAR(64) NOT NULL COMMENT '决策阶段',
  decision_title VARCHAR(128) NOT NULL COMMENT '决策标题',
  decision_reason TEXT COMMENT '决策原因',
  input_summary TEXT COMMENT '输入摘要',
  output_summary TEXT COMMENT '输出摘要',
  tool_name VARCHAR(128) COMMENT '工具名称',
  confidence DECIMAL(4,2) COMMENT '置信度',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_agent_decision_task (task_id),
  INDEX idx_agent_decision_stage (stage)
) COMMENT='Agent决策链表';

CREATE TABLE remediation_ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
  result_id BIGINT NOT NULL COMMENT '漏洞结果ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  title VARCHAR(200) NOT NULL COMMENT '工单标题',
  risk_level VARCHAR(32) COMMENT '风险等级',
  status VARCHAR(32) NOT NULL DEFAULT 'TODO' COMMENT '工单状态',
  assignee_id BIGINT COMMENT '负责人ID',
  assignee_name VARCHAR(64) COMMENT '负责人',
  fix_suggestion TEXT COMMENT '修复建议',
  retest_result TEXT COMMENT '复测结果',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_ticket_task (task_id),
  INDEX idx_ticket_result (result_id),
  INDEX idx_ticket_status (status)
) COMMENT='整改工单表';

CREATE TABLE security_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识ID',
  vuln_type VARCHAR(64) NOT NULL COMMENT '漏洞类型',
  title VARCHAR(160) NOT NULL COMMENT '知识标题',
  risk_level VARCHAR(32) COMMENT '默认风险等级',
  description TEXT COMMENT '漏洞说明',
  cause TEXT COMMENT '形成原因',
  impact TEXT COMMENT '影响范围',
  fix_advice TEXT COMMENT '修复建议',
  reference VARCHAR(512) COMMENT '参考资料',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_knowledge_type (vuln_type),
  INDEX idx_knowledge_risk (risk_level)
) COMMENT='安全知识库表';

CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审计日志ID',
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(64) COMMENT '用户名',
  role VARCHAR(32) COMMENT '角色',
  action VARCHAR(64) NOT NULL COMMENT '操作类型',
  module VARCHAR(64) NOT NULL COMMENT '模块',
  detail VARCHAR(1024) COMMENT '详情',
  ip VARCHAR(64) COMMENT 'IP地址',
  result VARCHAR(32) COMMENT '操作结果',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_audit_user (username),
  INDEX idx_audit_module (module),
  INDEX idx_audit_action (action),
  INDEX idx_audit_time (create_time)
) COMMENT='平台操作审计日志表';

CREATE TABLE challenge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
  title VARCHAR(160) NOT NULL COMMENT '题目标题',
  category VARCHAR(64) NOT NULL COMMENT '题目类型',
  difficulty VARCHAR(32) NOT NULL COMMENT '难度',
  score INT NOT NULL DEFAULT 10 COMMENT '积分',
  description TEXT COMMENT '题目背景和目标',
  target_url VARCHAR(512) COMMENT '模拟目标地址',
  request_method VARCHAR(16) DEFAULT 'GET' COMMENT '请求方法',
  request_example TEXT COMMENT '模拟请求信息',
  hint TEXT COMMENT '提示',
  answer_hash VARCHAR(128) NOT NULL COMMENT '答案SHA-256哈希',
  explanation TEXT COMMENT '答对后的解析',
  knowledge_id BIGINT COMMENT '关联知识库ID',
  status VARCHAR(32) NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  create_user_id BIGINT COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_challenge_category (category),
  INDEX idx_challenge_difficulty (difficulty),
  INDEX idx_challenge_status (status)
) COMMENT='趣味靶场题目表';

CREATE TABLE challenge_submit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
  challenge_id BIGINT NOT NULL COMMENT '题目ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(64) COMMENT '用户名',
  submit_answer VARCHAR(256) COMMENT '提交答案',
  correct TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确',
  score_got INT NOT NULL DEFAULT 0 COMMENT '获得积分',
  used_hint TINYINT NOT NULL DEFAULT 0 COMMENT '是否使用提示',
  submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  INDEX idx_submit_challenge (challenge_id),
  INDEX idx_submit_user (user_id),
  INDEX idx_submit_time (submit_time)
) COMMENT='趣味靶场提交记录表';

CREATE TABLE user_achievement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成就ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  achievement_code VARCHAR(64) NOT NULL COMMENT '成就编码',
  achievement_name VARCHAR(64) NOT NULL COMMENT '成就名称',
  achievement_desc VARCHAR(255) COMMENT '成就说明',
  icon VARCHAR(64) COMMENT '图标标识',
  unlock_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
  UNIQUE KEY uk_user_achievement (user_id, achievement_code)
) COMMENT='用户成就表';

CREATE TABLE user_score (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '积分ID',
  user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
  username VARCHAR(64) COMMENT '用户名',
  total_score INT NOT NULL DEFAULT 0 COMMENT '总积分',
  solved_count INT NOT NULL DEFAULT 0 COMMENT '已完成题目数',
  submit_count INT NOT NULL DEFAULT 0 COMMENT '提交次数',
  correct_rate DECIMAL(5,1) NOT NULL DEFAULT 0.0 COMMENT '正确率',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_score_total (total_score)
) COMMENT='用户积分表';

CREATE TABLE test_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  log_level VARCHAR(16) NOT NULL COMMENT '日志级别：INFO/WARN/ERROR/SUCCESS',
  message VARCHAR(1024) NOT NULL COMMENT '日志内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_log_task_id (task_id),
  INDEX idx_log_level (log_level),
  INDEX idx_log_created_at (created_at)
) COMMENT='测试日志表';

CREATE TABLE test_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  title VARCHAR(200) NOT NULL COMMENT '报告标题',
  content MEDIUMTEXT NOT NULL COMMENT '报告内容',
  format VARCHAR(32) NOT NULL DEFAULT 'MARKDOWN' COMMENT '报告格式',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_report_task_id (task_id)
) COMMENT='测试报告表';

INSERT INTO `user` (id, username, password, nickname, email, phone, role, status) VALUES
(1, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '系统管理员', 'admin@example.com', '13800000001', 'ADMIN', 'ENABLED'),
(2, 'user', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '安全测试员', 'user@example.com', '13800000002', 'USER', 'ENABLED');

INSERT INTO test_task
(id, task_name, target_url, test_type, description, plan, test_depth, generate_report, status, create_user_id, create_username, created_at, updated_at)
VALUES
(1, '演示靶场未授权访问测试', 'http://localhost:18080/api/demo-target', '未授权访问测试', '验证内置演示靶场管理接口是否存在未授权访问风险。', '1. 任务理解：确认目标地址与测试类型。\n2. 范围约束：仅在课程演示授权范围内模拟验证。\n3. 用例生成：检查匿名访问管理接口。\n4. 结果分析：评估响应敏感字段。\n5. 报告输出：生成修复建议。', '标准', 1, 'COMPLETED', 1, 'admin', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
(2, '课程演示弱口令测试', 'http://localhost:18080/api/demo-target', '弱口令测试', '验证演示账号是否存在常见弱口令组合。', '1. 加载弱口令规则模板。\n2. 限定演示账号范围。\n3. 模拟低频口令校验。\n4. 生成认证风险建议。', '快速', 1, 'COMPLETED', 1, 'admin', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(3, '订单接口水平越权测试', 'http://localhost:18080/api/demo-target', '越权访问测试', '验证订单详情接口是否根据登录态校验资源归属。', '1. 识别资源 ID 参数。\n2. 模拟修改 orderId/userId。\n3. 分析响应归属字段。\n4. 输出越权风险结论。', '标准', 1, 'COMPLETED', 1, 'admin', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(4, '我的资料接口敏感字段泄露测试', 'http://localhost:18080/api/demo-target', '敏感信息泄露测试', '普通用户验证资料接口是否返回手机号、token、内部路径等敏感字段。', '1. 请求资料接口。\n2. 识别敏感字段。\n3. 评估泄露影响。\n4. 生成脱敏建议。', '深入', 1, 'COMPLETED', 2, 'user', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(5, '我的支付接口参数篡改测试', 'http://localhost:18080/api/demo-target', '接口参数篡改测试', '普通用户验证 price、status 等关键参数是否由服务端校验。', '1. 捕获标准业务请求。\n2. 模拟篡改关键参数。\n3. 分析服务端校验结果。\n4. 输出业务安全建议。', '标准', 1, 'PENDING', 2, 'user', NOW(), NOW());

INSERT INTO test_result
(task_id, vulnerability_name, vulnerability_type, risk_level, url, description, reproduce_steps, request_example, response_example, suggestion, status, risk_score, impact_score, exploitability_score, difficulty_score, risk_vector, created_at)
VALUES
(1, '后台接口未授权访问', '访问控制', 'HIGH', 'http://localhost:18080/api/demo-target/admin/users', '未登录状态下访问管理接口仍返回敏感数据。', '1. 清空登录态。\n2. 请求管理接口。\n3. 观察响应数据。', 'GET /api/demo-target/admin/users HTTP/1.1\nHost: localhost:18080', 'HTTP/1.1 200 OK\n{"users":[{"username":"admin"}]}', '后端统一增加鉴权拦截器。', 'OPEN', 9.1, 9.7, 8.9, 1.9, 'SCORE:9.1;LEVEL:HIGH;PRIORITY:P1/优先修复;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 6 DAY),
(1, '接口错误信息暴露访问路径', '信息泄露', 'LOW', 'http://localhost:18080/api/demo-target/admin/config', '错误信息暴露内部权限标识。', '1. 匿名访问配置接口。\n2. 观察错误响应。', 'GET /api/admin/config HTTP/1.1', 'HTTP/1.1 403 Forbidden\n{"message":"admin:config:read required"}', '统一错误响应，隐藏内部权限标识。', 'OPEN', 3.2, 3.8, 3.6, 6.5, 'SCORE:3.2;LEVEL:LOW;PRIORITY:P3/例行优化;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 6 DAY),
(2, '系统存在弱口令账号', '身份认证', 'MEDIUM', 'http://localhost:18080/api/demo-target/login', '检测到账户使用常见弱密码组合。', '1. 限定授权账号。\n2. 尝试演示弱口令。\n3. 命中后停止。', 'POST /login\n{"username":"admin","password":"123456"}', 'HTTP/1.1 200 OK\n{"token":"***"}', '增加密码复杂度和失败锁定策略。', 'OPEN', 6.5, 7.1, 6.6, 4.2, 'SCORE:6.5;LEVEL:MEDIUM;PRIORITY:P2/计划修复;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 5 DAY),
(2, '登录失败提示可辅助账号枚举', '身份认证', 'LOW', 'http://localhost:18080/api/demo-target/login', '登录失败提示区分账号不存在和密码错误。', '1. 使用不存在账号登录。\n2. 使用存在账号错误密码。\n3. 对比提示。', 'POST /login\n{"username":"not_exists","password":"test"}', 'HTTP/1.1 401\n{"message":"用户不存在"}', '统一提示为用户名或密码错误。', 'OPEN', 2.4, 3.0, 3.6, 6.5, 'SCORE:2.4;LEVEL:LOW;PRIORITY:P3/例行优化;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 5 DAY),
(3, '订单详情接口存在水平越权', '访问控制', 'HIGH', 'http://localhost:18080/api/demo-target/orders/10086?userId=2', '修改 userId/orderId 后可访问其他用户订单。', '1. 用户A登录。\n2. 修改订单ID。\n3. 返回用户B订单。', 'GET /api/orders/10086?userId=2\nAuthorization: Bearer userA-token', 'HTTP/1.1 200 OK\n{"ownerUserId":2,"amount":399}', '根据登录态绑定用户身份并校验资源归属。', 'OPEN', 8.6, 9.2, 8.9, 1.9, 'SCORE:8.6;LEVEL:HIGH;PRIORITY:P1/优先修复;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 3 DAY),
(3, '管理接口缺少角色权限校验', '访问控制', 'MEDIUM', 'http://localhost:18080/api/demo-target/admin/config', '普通用户可访问部分管理配置接口。', '1. 普通用户登录。\n2. 访问管理接口。\n3. 返回配置数据。', 'GET /api/admin/config\nAuthorization: Bearer user-token', 'HTTP/1.1 200 OK\n{"systemMode":"debug"}', '建立 RBAC 权限模型。', 'OPEN', 7.4, 8.0, 8.4, 2.4, 'SCORE:7.4;LEVEL:MEDIUM;PRIORITY:P1/优先修复;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 3 DAY),
(4, '接口响应泄露敏感字段', '信息泄露', 'MEDIUM', 'http://localhost:18080/api/demo-target/profile', '响应中包含手机号、邮箱、token、内部路径等敏感信息。', '1. 登录后访问资料接口。\n2. 检查响应字段。\n3. 发现敏感字段。', 'GET /api/profile\nAuthorization: Bearer demo-token', 'HTTP/1.1 200 OK\n{"phone":"13812345678","token":"debug-token"}', '返回数据脱敏，避免暴露内部调试信息。', 'OPEN', 5.8, 6.4, 6.1, 4.2, 'SCORE:5.8;LEVEL:MEDIUM;PRIORITY:P2/计划修复;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 1 DAY),
(4, '异常页面暴露技术栈信息', '信息泄露', 'LOW', 'http://localhost:18080/api/demo-target/error', '异常响应包含框架版本和堆栈片段。', '1. 构造异常请求。\n2. 观察错误页面。\n3. 发现堆栈信息。', 'GET /api/detail?id=%27', 'HTTP/1.1 500\njava.lang.IllegalArgumentException', '关闭生产环境详细错误输出。', 'OPEN', 3.2, 3.8, 3.6, 6.5, 'SCORE:3.2;LEVEL:LOW;PRIORITY:P3/例行优化;MODEL:DEMO-CVSS-LITE', NOW() - INTERVAL 1 DAY),
(5, '接口参数缺少服务端校验', '业务安全', 'MEDIUM', 'http://localhost:18080/api/demo-target/update-role', '修改 price、role、status 等参数后服务端未校验。', '1. 捕获正常请求。\n2. 修改关键参数。\n3. 服务端接受异常值。', 'POST /api/payments\n{"orderId":7,"price":0.01,"status":"PAID"}', 'HTTP/1.1 200 OK\n{"paidAmount":0.01}', '关键参数由服务端计算或校验。', 'OPEN', 6.2, 6.8, 6.6, 3.7, 'SCORE:6.2;LEVEL:MEDIUM;PRIORITY:P2/计划修复;MODEL:DEMO-CVSS-LITE', NOW()),
(5, '搜索接口缺少参数白名单', '输入校验', 'LOW', 'http://localhost:18080/api/demo-target/search?debug=true', '接口接受未声明参数并返回调试信息。', '1. 追加 debug 参数。\n2. 对比响应差异。', 'GET /api/search?keyword=test&debug=true', 'HTTP/1.1 200 OK\n{"sql":"select * from demo"}', '增加参数白名单和类型校验。', 'OPEN', 2.4, 3.0, 3.6, 6.5, 'SCORE:2.4;LEVEL:LOW;PRIORITY:P3/例行优化;MODEL:DEMO-CVSS-LITE', NOW());

INSERT INTO test_log (task_id, log_level, message, created_at) VALUES
(1, 'INFO', 'Agent 已接收任务：演示靶场未授权访问测试', NOW() - INTERVAL 6 DAY),
(1, 'SUCCESS', '测试计划生成完成', NOW() - INTERVAL 6 DAY),
(1, 'INFO', '开始模拟匿名访问管理接口', NOW() - INTERVAL 6 DAY),
(1, 'WARN', '发现疑似未授权访问风险', NOW() - INTERVAL 6 DAY),
(1, 'SUCCESS', '报告生成完成', NOW() - INTERVAL 6 DAY),
(2, 'INFO', 'Agent 已接收任务：课程演示弱口令测试', NOW() - INTERVAL 5 DAY),
(2, 'SUCCESS', '弱口令规则模板加载完成', NOW() - INTERVAL 5 DAY),
(2, 'WARN', '检测到演示账号弱口令组合', NOW() - INTERVAL 5 DAY),
(2, 'SUCCESS', '认证风险分析完成', NOW() - INTERVAL 5 DAY),
(3, 'INFO', 'Agent 已接收任务：订单接口水平越权测试', NOW() - INTERVAL 3 DAY),
(3, 'INFO', '识别 orderId 与 userId 参数', NOW() - INTERVAL 3 DAY),
(3, 'WARN', '修改资源 ID 后响应归属发生变化', NOW() - INTERVAL 3 DAY),
(3, 'SUCCESS', '越权风险分析完成', NOW() - INTERVAL 3 DAY),
(4, 'INFO', 'Agent 已接收任务：用户信息敏感字段泄露测试', NOW() - INTERVAL 1 DAY),
(4, 'INFO', '开始识别响应敏感字段', NOW() - INTERVAL 1 DAY),
(4, 'WARN', '发现手机号、token 等敏感字段', NOW() - INTERVAL 1 DAY),
(4, 'SUCCESS', '敏感信息泄露测试完成', NOW() - INTERVAL 1 DAY),
(5, 'INFO', '任务已创建，等待执行', NOW()),
(5, 'INFO', '测试计划已生成', NOW()),
(5, 'SUCCESS', '演示数据初始化完成', NOW());

INSERT INTO agent_decision (task_id, step_no, stage, decision_title, decision_reason, input_summary, output_summary, tool_name, confidence, create_time) VALUES
(1, 1, 'TASK_UNDERSTANDING', '识别未授权访问测试意图', '目标包含 demo-target，Agent 判断本次测试为内置演示靶场授权范围内的未授权访问验证。', '目标：http://localhost:18080/api/demo-target；类型：未授权访问测试', '确认测试边界为课程演示环境。', 'TaskInterpreter', 0.94, NOW() - INTERVAL 6 DAY),
(1, 2, 'PLAN_GENERATION', '生成匿名访问检查计划', '未授权访问测试需要验证管理接口是否在无登录态下返回敏感数据。', '任务描述：验证内置演示靶场管理接口', '输出 5 步测试计划。', 'RuleBasedPlanGenerator', 0.91, NOW() - INTERVAL 6 DAY),
(1, 3, 'TOOL_SELECTION', '选择规则模拟执行器', '课程设计不执行真实攻击扫描，使用本地模拟请求样例保证演示安全。', '测试类型：未授权访问测试', '选择 RuleBasedTestExecutor。', 'ToolSelector', 0.88, NOW() - INTERVAL 6 DAY),
(1, 4, 'TEST_EXECUTION', '模拟访问管理接口', '根据规则构造匿名请求样例，识别响应中用户字段。', 'GET /api/demo-target/admin/users', '生成未授权访问疑似风险。', 'RuleBasedTestExecutor', 0.90, NOW() - INTERVAL 6 DAY),
(1, 5, 'RESULT_ANALYSIS', '判定高危风险', '未登录访问管理接口可能导致用户信息泄露，因此评分 9.1。', '响应包含 username、role、phone', '输出 HIGH 和修复建议。', 'RiskScoreService', 0.87, NOW() - INTERVAL 6 DAY),
(1, 6, 'REPORT_GENERATION', '生成测试报告', '将计划、日志、漏洞和风险评分汇总为课程设计报告。', '漏洞结果 2 条', '生成 Markdown/Word 可导出报告。', 'MarkdownReportGenerator', 0.93, NOW() - INTERVAL 6 DAY),
(3, 1, 'TASK_UNDERSTANDING', '识别越权访问测试意图', 'Agent 识别 orderId/userId 属于资源归属校验场景。', '目标：订单接口；类型：越权访问测试', '确认水平越权验证目标。', 'TaskInterpreter', 0.95, NOW() - INTERVAL 3 DAY),
(3, 2, 'PLAN_GENERATION', '生成资源 ID 篡改计划', '越权测试需要对比不同资源 ID 的响应归属字段。', '任务描述：订单详情接口', '生成参数篡改与响应对比步骤。', 'RuleBasedPlanGenerator', 0.92, NOW() - INTERVAL 3 DAY),
(3, 3, 'TOOL_SELECTION', '选择越权规则模板', '使用本地演示模板模拟 userId/orderId 变化，不发起真实攻击。', '测试类型：越权访问测试', '选择访问控制规则模板。', 'ToolSelector', 0.89, NOW() - INTERVAL 3 DAY),
(3, 4, 'TEST_EXECUTION', '模拟订单 ID 修改', '通过响应 ownerUserId 变化判断可能存在水平越权。', 'GET /orders/10086?userId=2', '发现归属字段异常变化。', 'RuleBasedTestExecutor', 0.90, NOW() - INTERVAL 3 DAY),
(3, 5, 'RESULT_ANALYSIS', '输出高危越权风险', '水平越权可直接访问他人业务数据，风险评分 8.6。', '响应包含其他用户订单金额', '输出 HIGH 风险与绑定登录态建议。', 'RiskScoreService', 0.88, NOW() - INTERVAL 3 DAY),
(3, 6, 'REPORT_GENERATION', '生成越权测试报告', '整合越权验证路径、评分和整改建议。', '漏洞结果 2 条', '生成正式报告。', 'MarkdownReportGenerator', 0.93, NOW() - INTERVAL 3 DAY);

INSERT INTO remediation_ticket (id, result_id, task_id, title, risk_level, status, assignee_id, assignee_name, fix_suggestion, retest_result, create_time, update_time) VALUES
(1, 1, 1, '整改工单：后台接口未授权访问', 'HIGH', 'FIXING', 1, '系统管理员', '后端统一增加鉴权拦截器。', '尚未复测', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 2 DAY),
(2, 5, 3, '整改工单：订单详情接口存在水平越权', 'HIGH', 'CONFIRMED', 1, '系统管理员', '根据登录态绑定用户身份并校验资源归属。', '尚未复测', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY),
(3, 7, 4, '整改工单：接口响应泄露敏感字段', 'MEDIUM', 'TODO', 2, '安全测试员', '返回数据脱敏，避免暴露内部调试信息。', '尚未复测', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

INSERT INTO security_knowledge (vuln_type, title, risk_level, description, cause, impact, fix_advice, reference, create_time) VALUES
('未授权访问', '后台接口未授权访问', 'HIGH', '未登录或低权限用户可以访问本应受保护的后台接口。', '缺少统一鉴权拦截器或接口白名单配置错误。', '可能泄露用户、配置、业务数据，甚至导致管理功能被滥用。', '后端统一接入鉴权拦截器，对管理接口校验登录态和角色权限。', 'OWASP Broken Access Control', NOW()),
('水平越权', '订单详情接口水平越权', 'HIGH', '用户通过修改资源 ID 访问其他用户数据。', '服务端信任前端传入 userId/orderId，未校验资源归属。', '可能导致订单、资料等敏感业务数据泄露。', '服务端基于登录态查询资源归属，禁止信任前端身份参数。', 'OWASP IDOR/BOLA', NOW()),
('垂直越权', '普通用户访问管理功能', 'HIGH', '低权限用户访问高权限接口或功能。', '仅校验登录态，未校验角色和权限点。', '可能导致配置泄露、用户管理越权或业务破坏。', '建立 RBAC 权限模型，对管理接口增加 ADMIN 角色校验。', 'OWASP Broken Access Control', NOW()),
('弱口令', '系统存在弱口令账号', 'MEDIUM', '账号使用 123456、password 等常见密码。', '缺少密码复杂度校验和初始密码修改机制。', '攻击者可能低频猜测获得账号权限。', '启用密码复杂度、失败锁定、初始密码强制修改和 MFA。', 'OWASP Authentication', NOW()),
('敏感信息泄露', '接口响应泄露敏感字段', 'MEDIUM', '响应包含手机号、邮箱、token、内部路径或调试字段。', '接口返回字段未按最小必要原则裁剪。', '敏感数据可能被收集并用于进一步攻击。', '返回字段脱敏，统一异常响应，关闭生产调试信息。', 'OWASP Sensitive Data Exposure', NOW()),
('参数篡改', '接口参数缺少服务端校验', 'MEDIUM', '修改 price、role、status 等关键参数后服务端接受异常值。', '关键业务参数由前端控制，服务端未二次计算或校验。', '可能导致金额异常、权限提升或流程绕过。', '关键参数由服务端计算，增加参数白名单和业务规则校验。', 'Business Logic Security', NOW()),
('Token 泄露', '接口或日志泄露 Token', 'HIGH', 'Token 出现在响应体、URL、日志或错误信息中。', '调试信息未清理或认证信息被错误返回。', '可能导致会话被冒用。', 'Token 不入 URL，不写普通日志，响应中禁止返回调试 token。', 'OWASP Session Management', NOW()),
('服务端校验', '接口缺少服务端校验', 'MEDIUM', '接口仅依赖前端校验，服务端缺少类型、范围和权限校验。', '开发时过度信任前端表单和 UI 限制。', '可能出现越权、参数篡改和业务绕过。', '所有关键校验在服务端实现，前端校验只作为体验优化。', 'Secure Coding Guideline', NOW());

INSERT INTO challenge
(id, title, category, difficulty, score, description, target_url, request_method, request_example, hint, answer_hash, explanation, knowledge_id, status, create_user_id, create_time, update_time)
VALUES
(1, '神秘公开接口', '信息泄露', '简单', 10,
 '内置演示靶场的公开信息接口返回了不应该出现在公开响应中的调试字段。请观察响应内容，找到泄露的 flag。',
 'http://localhost:18080/api/demo-target/public/info', 'GET',
 'GET /api/demo-target/public/info HTTP/1.1\nHost: localhost:18080',
 '重点观察 public/info 响应中看起来像演示标记或调试字段的内容。',
 '6e694a208228f130be8ec87ebd6592096b98f1da4ed3041601cb733c813e8d2a',
 '公开接口不应返回 flag、调试 token 或内部路径。该题答案来自 public/info 模拟响应中的 leakedFlag 字段。',
 5, 'ENABLED', 1, NOW(), NOW()),
(2, '越权的订单', '水平越权', '简单', 15,
 '订单查询接口允许通过 orderId 和 userId 查看不同用户订单。请根据题面提示找出属于其他用户的订单标识。',
 'http://localhost:18080/api/demo-target/orders/1002?userId=2', 'GET',
 'GET /api/demo-target/orders/1002?userId=2 HTTP/1.1\nHost: localhost:18080',
 '水平越权常见线索是 orderId、userId 等资源归属参数被前端控制。',
 '13a3cd1c78168ab5cab11d92e8e5d08e75cdbcf3dad8ed55ae0167bdec5dba08',
 '当 orderId=1002 且 userId=2 时，模拟接口返回其他用户订单标识，体现 IDOR/BOLA 风险。',
 2, 'ENABLED', 1, NOW(), NOW()),
(3, '弱口令账号', '弱口令', '简单', 10,
 '系统中存在使用常见弱口令的演示账号。请根据题面和模拟登录接口判断弱口令风险对应的 flag。',
 'http://localhost:18080/api/demo-target/login', 'POST',
 'POST /api/demo-target/login?username=admin&password=admin123 HTTP/1.1',
 '关注常见默认账号和弱密码组合，例如 admin/admin123。',
 '08e64b1e857f72b9a6857da0899cd54c15c16f6b1e52d73c8bbdbba0e3904cec',
 '弱口令不是要爆破真实系统，而是通过课程演示数据理解默认密码治理的重要性。',
 4, 'ENABLED', 1, NOW(), NOW()),
(4, '参数篡改', '接口参数篡改', '中等', 20,
 'update-role 模拟接口接受前端传入的 role 参数。请判断哪个参数不应该由前端直接控制。',
 'http://localhost:18080/api/demo-target/update-role', 'POST',
 'POST /api/demo-target/update-role?userId=2&role=ADMIN HTTP/1.1',
 '权限、价格、状态等关键字段应由服务端计算或校验，不能相信前端传参。',
 'b9cf962e47891a9771ea3a41be8b3d5b9fcf795c379d41681341ff3195420e01',
 'role 表示用户角色，若由前端直接传入并被服务端接受，可能导致权限提升。',
 6, 'ENABLED', 1, NOW(), NOW()),
(5, 'Token 泄露', '敏感信息泄露', '中等', 20,
 '用户资料模拟接口返回了 debugToken。请识别不应该返回给前端的敏感字段。',
 'http://localhost:18080/api/demo-target/profile?userId=1', 'GET',
 'GET /api/demo-target/profile?userId=1 HTTP/1.1\nHost: localhost:18080',
 'Token、内部路径、调试字段都不应该出现在普通前端响应中。',
 '5b37cd392915e2922122b228651eae5c3144c594a0f3e8dce446515353206004',
 'debugToken 属于敏感认证或调试信息，泄露后可能被用于会话冒用或进一步分析。',
 7, 'ENABLED', 1, NOW(), NOW()),
(6, 'Agent 推理题', 'Agent 推理', '简单', 15,
 '观察 Agent 决策链，思考为什么系统会把未授权访问判定为高危风险。',
 'http://localhost:18080/api/tasks/1/agent-decisions', 'GET',
 'GET /api/tasks/1/agent-decisions HTTP/1.1\nAuthorization: Bearer <token>',
 '查看 Agent 决策链第 5 步 RESULT_ANALYSIS：未登录访问管理接口可能导致用户信息泄露，因此评分 9.1。根据题目要求提交格式为 flag{unauthorized_high_risk}。',
 'd3c34759391465e69e94a51e59a22af0359fc60d2fa9e1e5b4804ce56ba4b1ee',
 '本题答案为 flag{unauthorized_high_risk}。Agent 将未授权访问判为高危，是因为未登录即可访问管理接口，可能直接泄露用户、角色或配置数据，利用门槛低、影响范围大。',
 1, 'ENABLED', 1, NOW(), NOW());

INSERT INTO user_score (user_id, username, total_score, solved_count, submit_count, correct_rate, update_time) VALUES
(1, 'admin', 0, 0, 0, 0.0, NOW()),
(2, 'user', 0, 0, 0, 0.0, NOW());

INSERT INTO system_config (config_key, config_value, config_label, update_time) VALUES
('platform_name', '自主安全测试智能体可视化平台', '平台名称', NOW()),
('agent_mode', '规则模拟模式', 'Agent 模式', NOW()),
('default_depth', '标准', '默认测试深度', NOW()),
('auto_report', 'true', '自动生成报告', NOW()),
('enable_register', 'true', '开启注册', NOW()),
('security_statement', '本平台仅用于本地靶场、自建测试系统、课程演示环境或已授权目标，不执行未授权攻击或破坏性扫描。', '安全声明', NOW());

INSERT INTO audit_log (user_id, username, role, action, module, detail, ip, result, create_time) VALUES
(1, 'admin', 'ADMIN', 'LOGIN', '认证中心', '管理员登录系统', 'local', 'SUCCESS', NOW() - INTERVAL 1 DAY),
(1, 'admin', 'ADMIN', 'EXECUTE_TASK', 'Agent执行', '执行演示靶场未授权访问测试', 'local', 'SUCCESS', NOW() - INTERVAL 1 DAY),
(1, 'admin', 'ADMIN', 'CREATE_TICKET', '整改工单', '从高危漏洞生成整改工单', 'local', 'SUCCESS', NOW() - INTERVAL 1 DAY),
(2, 'user', 'USER', 'LOGIN', '认证中心', '普通用户登录系统', 'local', 'SUCCESS', NOW()),
(2, 'user', 'USER', 'CREATE_TASK', '测试任务', '创建我的支付接口参数篡改测试', 'local', 'SUCCESS', NOW());

INSERT INTO test_report (task_id, title, content, format, created_at) VALUES
(1, '课程设计演示用安全测试报告 - 演示靶场未授权访问测试', '# 课程设计演示用安全测试报告 - 演示靶场未授权访问测试\n\n## 1. 测试任务信息\n\n- 测试目标：http://localhost:8081\n- 测试类型：未授权访问测试\n\n## 2. 风险统计\n\n- HIGH：1\n- LOW：1\n\n## 3. 漏洞详情\n\n### 后台接口未授权访问\n未登录状态下访问管理接口仍返回敏感数据。\n\n修复建议：后端统一增加鉴权拦截器。\n\n## 4. 结论\n本报告由规则模拟版 Agent 生成，用于课程设计演示。', 'MARKDOWN', NOW() - INTERVAL 6 DAY),
(3, '课程设计演示用安全测试报告 - 订单接口水平越权测试', '# 课程设计演示用安全测试报告 - 订单接口水平越权测试\n\n## 1. 测试任务信息\n\n- 测试目标：http://localhost:8081\n- 测试类型：越权访问测试\n\n## 2. 风险统计\n\n- HIGH：1\n- MEDIUM：1\n\n## 3. 漏洞详情\n\n### 订单详情接口存在水平越权\n修改 userId/orderId 后可访问其他用户订单。\n\n修复建议：根据登录态绑定用户身份并校验资源归属。\n\n## 4. 结论\n系统应加强资源归属校验和 RBAC 权限控制。', 'MARKDOWN', NOW() - INTERVAL 3 DAY),
(4, '课程设计演示用安全测试报告 - 用户信息敏感字段泄露测试', '# 课程设计演示用安全测试报告 - 用户信息敏感字段泄露测试\n\n## 1. 测试任务信息\n\n- 测试目标：http://localhost:8081\n- 测试类型：敏感信息泄露测试\n\n## 2. 风险统计\n\n- MEDIUM：1\n- LOW：1\n\n## 3. 漏洞详情\n\n### 接口响应泄露敏感字段\n响应中包含手机号、邮箱、token、内部路径等敏感信息。\n\n修复建议：返回数据脱敏，避免暴露内部调试信息。\n\n## 4. 结论\n系统需按最小必要原则返回字段并统一异常响应。', 'MARKDOWN', NOW() - INTERVAL 1 DAY);
