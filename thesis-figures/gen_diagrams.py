"""
CampusLink 论文图表生成脚本
生成：用例图、ER图、流程图（注册登录/Token刷新/资源上传/任务状态机）、时序图（OSS直传/Token并发刷新）
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyArrowPatch, FancyBboxPatch, Arc
from matplotlib.lines import Line2D
import numpy as np

OUT = "/Users/chasexu/XuProject/CampusLink/thesis-figures"
plt.rcParams['font.family'] = ['Heiti TC', 'STHeiti', 'Hiragino Sans GB', 'Songti SC', 'sans-serif']
plt.rcParams['axes.unicode_minus'] = False

# ── 灰色调色板 ──────────────────────────────────────────────
G_BG      = '#F7F7F7'   # 页面背景
G_BOX_F   = '#ECECEC'   # 普通框填充
G_BOX_E   = '#555555'   # 普通框边框
G_DIA_F   = '#DCDCDC'   # 菱形填充
G_DIA_E   = '#666666'   # 菱形边框
G_TERM_F  = '#D0D0D0'   # 终止椭圆填充
G_TERM_E  = '#333333'   # 终止椭圆边框
G_HDR_F   = '#4A4A4A'   # 深色标题框填充
G_HDR_E   = '#333333'   # 深色标题框边框
G_OK_F    = '#C8C8C8'   # 成功/完成框填充
G_OK_E    = '#444444'   # 成功/完成框边框
G_ERR_F   = '#B8B8B8'   # 错误/取消框填充
G_ERR_E   = '#777777'   # 错误/取消框边框
G_ARR     = '#444444'   # 箭头颜色
G_NOTE_F  = '#E5E5E5'   # 注释框填充
G_NOTE_E  = '#888888'   # 注释框边框

# ──────────────────────────────────────────────────────────────
# 工具函数
# ──────────────────────────────────────────────────────────────
def box(ax, x, y, w, h, text, fc=None, ec=None, fontsize=9, radius=0.02, bold=False):
    if fc is None: fc = G_BOX_F
    if ec is None: ec = G_BOX_E
    rect = FancyBboxPatch((x - w/2, y - h/2), w, h,
                          boxstyle=f"round,pad={radius}", fc=fc, ec=ec, lw=1.2, zorder=3)
    ax.add_patch(rect)
    weight = 'bold' if bold else 'normal'
    ax.text(x, y, text, ha='center', va='center', fontsize=fontsize, weight=weight, zorder=4)

def diamond(ax, x, y, w, h, text, fc=G_DIA_F, ec=G_DIA_E, fontsize=8.5):
    pts = np.array([[x, y+h/2],[x+w/2, y],[x, y-h/2],[x-w/2, y]])
    poly = plt.Polygon(pts, fc=fc, ec=ec, lw=1.2, zorder=3)
    ax.add_patch(poly)
    ax.text(x, y, text, ha='center', va='center', fontsize=fontsize, zorder=4)

def arrow(ax, x1, y1, x2, y2, label='', color='#555', lw=1.2, style='->', curved=False):
    if curved:
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle=style, color=color, lw=lw,
                                   connectionstyle='arc3,rad=0.25'), zorder=2)
    else:
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle=style, color=color, lw=lw), zorder=2)
    if label:
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx+0.02, my+0.02, label, fontsize=7.5, color='#333', zorder=5)

def oval(ax, x, y, rx, ry, text, fc=G_BOX_F, ec=G_BOX_E, fontsize=8.5):
    ell = mpatches.Ellipse((x, y), rx*2, ry*2, fc=fc, ec=ec, lw=1.2, zorder=3)
    ax.add_patch(ell)
    ax.text(x, y, text, ha='center', va='center', fontsize=fontsize, zorder=4)

def stick_figure(ax, x, y, label, size=0.06):
    """简单小人"""
    head = plt.Circle((x, y+size*1.8), size*0.55, fc='white', ec='#333333', lw=1.2, zorder=3)
    ax.add_patch(head)
    ax.plot([x, x], [y+size*1.2, y-size*0.4], color='#333333', lw=1.2, zorder=3)
    ax.plot([x-size, x+size], [y+size*0.6, y+size*0.6], color='#333333', lw=1.2, zorder=3)
    ax.plot([x, x-size*0.7], [y-size*0.4, y-size*1.4], color='#333333', lw=1.2, zorder=3)
    ax.plot([x, x+size*0.7], [y-size*0.4, y-size*1.4], color='#333333', lw=1.2, zorder=3)
    ax.text(x, y-size*1.9, label, ha='center', va='top', fontsize=8.5, weight='bold', zorder=4)

def save(fig, name):
    fig.savefig(f"{OUT}/{name}", dpi=150, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f"  saved: {name}")

# ══════════════════════════════════════════════════════════════
# 1. 用例图
# ══════════════════════════════════════════════════════════════
def draw_usecase():
    fig, ax = plt.subplots(figsize=(14, 9))
    ax.set_xlim(0, 14); ax.set_ylim(0, 9)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(7, 8.6, 'CampusLink 系统用例图', ha='center', va='center', fontsize=13, weight='bold')

    # 系统边界
    sys_box = FancyBboxPatch((2.5, 0.4), 9, 7.8, boxstyle="round,pad=0.1",
                              fc='#F2F2F2', ec='#555555', lw=1.5, zorder=1)
    ax.add_patch(sys_box)
    ax.text(7, 8.0, 'CampusLink 系统', ha='center', va='center', fontsize=10, color='#333333', weight='bold')

    # 角色
    stick_figure(ax, 0.9, 5.8, '普通用户\n(学生/教师)', size=0.07)
    stick_figure(ax, 0.9, 2.8, '社团管理员', size=0.07)
    stick_figure(ax, 13.1, 4.3, '系统管理员', size=0.07)

    # 普通用户用例
    user_cases = [
        (4.5, 7.2, '注册 / 登录'),
        (4.5, 6.4, '浏览 / 搜索资源'),
        (4.5, 5.6, '上传资源'),
        (4.5, 4.8, '下载资源'),
        (4.5, 4.0, '发布问题'),
        (4.5, 3.2, '回答问题'),
        (4.5, 2.4, '使用 AI 助手'),
        (4.5, 1.6, '发布 / 接受任务'),
        (7.5, 7.2, '报名 / 签到活动'),
        (7.5, 6.4, '私信聊天'),
        (7.5, 5.6, '查看通知'),
        (7.5, 4.8, '积分明细 / 签到'),
        (7.5, 4.0, '个人中心'),
    ]
    for (x, y, t) in user_cases:
        oval(ax, x, y, 1.55, 0.32, t, fc='#ECECEC', ec='#555555')
        if x < 5.5:
            arrow(ax, 1.3, 5.8, x-1.55, y, color='#555')

    # 社团管理员用例（继承普通用户）
    club_cases = [
        (7.5, 3.2, '创建 / 管理社团'),
        (7.5, 2.4, '发布活动'),
        (7.5, 1.6, '管理社团成员'),
    ]
    for (x, y, t) in club_cases:
        oval(ax, x, y, 1.55, 0.32, t, fc='#D8D8D8', ec='#444444')
        arrow(ax, 1.3, 2.8, x-1.55, y, color='#555')

    # 系统管理员用例
    admin_cases = [
        (10.5, 6.8, '用户管理'),
        (10.5, 6.0, '内容审核'),
        (10.5, 5.2, '举报处理'),
        (10.5, 4.4, '数据统计'),
        (10.5, 3.6, '积分 / 等级配置'),
        (10.5, 2.8, '公告推送'),
        (10.5, 2.0, '操作日志审计'),
    ]
    for (x, y, t) in admin_cases:
        oval(ax, x, y, 1.55, 0.32, t, fc='#C8C8C8', ec='#666666')
        arrow(ax, 12.7, 4.3, x+1.55, y, color='#555')

    # 社团管理员继承普通用户（虚线）
    ax.annotate('', xy=(0.9, 4.2), xytext=(0.9, 5.3),
                arrowprops=dict(arrowstyle='->', color='#888', lw=1, linestyle='dashed'), zorder=2)
    ax.text(1.2, 4.75, '«extend»', fontsize=7, color='#888', style='italic')

    # 图例
    legend_items = [
        mpatches.Patch(fc='#ECECEC', ec='#555555', label='普通用户用例'),
        mpatches.Patch(fc='#D8D8D8', ec='#444444', label='社团管理员用例'),
        mpatches.Patch(fc='#C8C8C8', ec='#666666', label='系统管理员用例'),
    ]
    ax.legend(handles=legend_items, loc='lower right', fontsize=8, framealpha=0.8)

    save(fig, '01_usecase.png')

# ══════════════════════════════════════════════════════════════
# 2. ER 图（核心表关系）
# ══════════════════════════════════════════════════════════════
def draw_er():
    fig, ax = plt.subplots(figsize=(16, 11))
    ax.set_xlim(0, 16); ax.set_ylim(0, 11)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(8, 10.6, 'CampusLink 数据库核心 ER 图', ha='center', va='center', fontsize=13, weight='bold')

    def er_box(x, y, title, fields, w=2.8, fc='#ECECEC', ec='#555555'):
        row_h = 0.32
        h = row_h * (len(fields) + 1) + 0.1
        # 标题行
        title_rect = FancyBboxPatch((x-w/2, y-row_h/2), w, row_h,
                                    boxstyle="round,pad=0.01", fc=ec, ec=ec, lw=0, zorder=3)
        ax.add_patch(title_rect)
        ax.text(x, y, title, ha='center', va='center', fontsize=8.5, weight='bold',
                color='white', zorder=4)
        # 字段行
        for i, (fname, ftype) in enumerate(fields):
            fy = y - row_h*(i+1)
            row_fc = '#F2F2F2' if i % 2 == 0 else 'white'
            row_rect = plt.Rectangle((x-w/2, fy-row_h/2), w, row_h,
                                      fc=row_fc, ec='#AAAAAA', lw=0.5, zorder=3)
            ax.add_patch(row_rect)
            key_mark = '🔑 ' if ftype.startswith('PK') else ('🔗 ' if ftype.startswith('FK') else '   ')
            ax.text(x-w/2+0.12, fy, f"{fname}", ha='left', va='center', fontsize=7.5, zorder=4)
            ax.text(x+w/2-0.08, fy, ftype.replace('PK','').replace('FK','').strip(),
                    ha='right', va='center', fontsize=6.8, color='#777', zorder=4)
            if ftype.startswith('PK'):
                ax.text(x-w/2+0.02, fy, '■', ha='left', va='center', fontsize=6, color='#333333', zorder=4)
            elif ftype.startswith('FK'):
                ax.text(x-w/2+0.02, fy, '◆', ha='left', va='center', fontsize=6, color='#666666', zorder=4)
        # 外框
        full_rect = plt.Rectangle((x-w/2, y-h+row_h/2), w, h,
                                    fc='none', ec=ec, lw=1.2, zorder=5)
        ax.add_patch(full_rect)
        return y - row_h*(len(fields)) - row_h/2  # bottom y

    # 表定义 (x, y, title, fields)
    tables = [
        (2.5, 10.1, 'users', [
            ('uid', 'PK bigint'),
            ('username', 'varchar'),
            ('role', 'varchar'),
            ('points', 'int'),
            ('status', 'tinyint'),
        ]),
        (7.5, 10.1, 'resources', [
            ('rid', 'PK bigint'),
            ('uploader_uid', 'FK bigint'),
            ('title', 'varchar'),
            ('status', 'tinyint'),
            ('download_count', 'int'),
        ]),
        (13.0, 10.1, 'questions', [
            ('qid', 'PK bigint'),
            ('user_uid', 'FK bigint'),
            ('title', 'varchar'),
            ('reward_points', 'int'),
            ('status', 'tinyint'),
        ]),
        (2.5, 6.5, 'answers', [
            ('aid', 'PK bigint'),
            ('qid', 'FK bigint'),
            ('user_uid', 'FK bigint'),
            ('is_adopted', 'tinyint'),
        ]),
        (7.5, 6.5, 'tasks', [
            ('tid', 'PK bigint'),
            ('publisher_uid', 'FK bigint'),
            ('taker_uid', 'FK bigint'),
            ('reward_points', 'int'),
            ('status', 'tinyint'),
        ]),
        (13.0, 6.5, 'clubs', [
            ('cid', 'PK bigint'),
            ('creator_uid', 'FK bigint'),
            ('name', 'varchar'),
        ]),
        (2.5, 3.5, 'notifications', [
            ('nid', 'PK bigint'),
            ('user_uid', 'FK bigint'),
            ('notify_type', 'varchar'),
            ('is_read', 'tinyint'),
        ]),
        (7.5, 3.5, 'points_history', [
            ('id', 'PK bigint'),
            ('user_uid', 'FK bigint'),
            ('points_change', 'int'),
            ('related_type', 'varchar'),
        ]),
        (13.0, 3.5, 'activities', [
            ('act_id', 'PK bigint'),
            ('club_cid', 'FK bigint'),
            ('creator_uid', 'FK bigint'),
            ('max_count', 'int'),
            ('status', 'tinyint'),
        ]),
        (7.5, 1.0, 'messages', [
            ('mid', 'PK bigint'),
            ('from_uid', 'FK bigint'),
            ('to_uid', 'FK bigint'),
            ('content', 'text'),
            ('is_read', 'tinyint'),
        ]),
    ]

    bottoms = {}
    for t in tables:
        x, y, name, fields = t
        b = er_box(x, y, name, fields)
        bottoms[name] = (x, y, b)

    # 关系连线 (from_table, to_table, label, from_side, to_side)
    rels = [
        ('users', 'resources', '1:N 上传', 'right', 'left'),
        ('users', 'questions', '1:N 发布', 'right', 'left'),
        ('users', 'answers', '1:N 回答', 'bottom', 'top'),
        ('questions', 'answers', '1:N', 'bottom', 'top'),
        ('users', 'tasks', '1:N 发布', 'bottom', 'top'),
        ('clubs', 'activities', '1:N 举办', 'bottom', 'top'),
        ('users', 'notifications', '1:N', 'bottom', 'top'),
        ('users', 'points_history', '1:N', 'right', 'left'),
        ('users', 'messages', '1:N 发送', 'bottom', 'top'),
    ]

    def get_anchor(name, side):
        x, y, b = bottoms[name]
        w = 2.8
        row_h = 0.32
        if side == 'right': return (x + w/2, y - row_h/2)
        if side == 'left':  return (x - w/2, y - row_h/2)
        if side == 'top':   return (x, y + row_h/2)
        if side == 'bottom': return (x, b)

    for (t1, t2, lbl, s1, s2) in rels:
        p1 = get_anchor(t1, s1)
        p2 = get_anchor(t2, s2)
        ax.annotate('', xy=p2, xytext=p1,
                    arrowprops=dict(arrowstyle='->', color='#888888', lw=1.0,
                                   connectionstyle='arc3,rad=0.0'), zorder=2)
        mx, my = (p1[0]+p2[0])/2, (p1[1]+p2[1])/2
        ax.text(mx+0.05, my+0.08, lbl, fontsize=6.5, color='#555', zorder=5)

    # 图例
    pk_patch = mpatches.Patch(fc='#555555', label='■ 主键 PK')
    fk_patch = mpatches.Patch(fc='#888888', label='◆ 外键 FK')
    ax.legend(handles=[pk_patch, fk_patch], loc='lower left', fontsize=8, framealpha=0.8)

    save(fig, '02_er_diagram.png')

# ══════════════════════════════════════════════════════════════
# 3. 用户注册登录流程图
# ══════════════════════════════════════════════════════════════
def draw_login_flow():
    # 宽度 11，注册列 x=2.2，登录列 x=7.8，错误框严格在各自半区内
    fig, ax = plt.subplots(figsize=(11, 11))
    ax.set_xlim(0, 11); ax.set_ylim(3.3, 12)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(5.5, 11.65, '用户注册 / 登录流程图', ha='center', fontsize=12, weight='bold')

    LX, RX = 2.2, 7.8   # 注册列、登录列中心
    DIV = 5.5             # 分隔线
    BW, BH = 2.4, 0.52
    DW, DH = 2.4, 0.56

    def rbox(x, y, text, **kw): box(ax, x, y, BW, BH, text, **kw)
    def dmd(x, y, text): diamond(ax, x, y, DW, DH, text)
    def term(x, y, text):
        e = mpatches.Ellipse((x, y), 2.1, 0.46, fc=G_TERM_F, ec=G_TERM_E, lw=1.2, zorder=3)
        ax.add_patch(e)
        ax.text(x, y, text, ha='center', va='center', fontsize=9, weight='bold', zorder=4)
    def err_r(x, y, text):   # 右侧小错误框
        box(ax, x, y, 1.8, BH, text, fc=G_ERR_F, ec=G_ERR_E, fontsize=8)

    # ══ 注册流程（左列）══
    ax.text(LX, 11.3, '注  册', ha='center', fontsize=10, weight='bold', color='#333333')
    term(LX, 10.9, '开 始')
    arrow(ax, LX, 10.66, LX, 10.18)
    rbox(LX, 9.92, '填写用户名/密码/邮箱')
    arrow(ax, LX, 9.66, LX, 9.18)
    rbox(LX, 8.92, '发送邮箱验证码\n(Redis TTL 5min)', fontsize=8)
    arrow(ax, LX, 8.66, LX, 8.18)
    dmd(LX, 7.9, '验证码是否\n正确且未过期?')
    # 否 → 右侧错误框（x=4.3，不超过分隔线 5.5）
    ax.annotate('', xy=(3.4, 7.9), xytext=(LX+DW/2, 7.9),
                arrowprops=dict(arrowstyle='->', color=G_ARR, lw=1.2), zorder=2)
    ax.text(LX+DW/2+0.08, 7.96, '否', ha='left', fontsize=8, color='#333', zorder=5)
    err_r(4.3, 7.9, '验证码错误')
    # 回环：从错误框顶部向上回到发送验证码框
    ax.annotate('', xy=(4.3, 8.92), xytext=(4.3, 8.17),
                arrowprops=dict(arrowstyle='->', color=G_ARR, lw=1.0), zorder=2)
    ax.annotate('', xy=(LX+BW/2, 8.92), xytext=(4.3, 8.92),
                arrowprops=dict(arrowstyle='->', color=G_ARR, lw=1.0), zorder=2)
    # 是 ↓
    arrow(ax, LX, 7.62, LX, 7.14, label='是')
    dmd(LX, 6.86, '用户名是否\n已存在?')
    # 是 → 右侧错误框
    ax.annotate('', xy=(3.4, 6.86), xytext=(LX+DW/2, 6.86),
                arrowprops=dict(arrowstyle='->', color=G_ARR, lw=1.2), zorder=2)
    ax.text(LX+DW/2+0.08, 6.92, '是', ha='left', fontsize=8, color='#333', zorder=5)
    err_r(4.3, 6.86, '用户名冲突')
    # 否 ↓
    arrow(ax, LX, 6.58, LX, 6.1, label='否')
    rbox(LX, 5.84, 'MD5 哈希密码')
    arrow(ax, LX, 5.58, LX, 5.1)
    rbox(LX, 4.84, '创建用户记录\n发放注册积分+100', fontsize=8)
    arrow(ax, LX, 4.58, LX, 4.1)
    term(LX, 3.84, '注册成功')

    # ══ 登录流程（右列）══
    ax.text(RX, 11.3, '登  录', ha='center', fontsize=10, weight='bold', color='#333333')
    term(RX, 10.9, '开 始')
    arrow(ax, RX, 10.66, RX, 10.18)
    rbox(RX, 9.92, '输入用户名 / 密码')
    arrow(ax, RX, 9.66, RX, 9.18)
    dmd(RX, 8.9, '用户是否存在?')
    arrow(ax, RX, 8.62, RX, 8.14, label='是')
    dmd(RX, 7.86, '密码哈希\n是否匹配?')
    arrow(ax, RX, 7.58, RX, 7.1, label='是')
    dmd(RX, 6.82, '账号是否\n正常(status=1)?')
    arrow(ax, RX, 6.54, RX, 6.06, label='是')
    rbox(RX, 5.8, '生成 AccessToken(2h)\n+ RefreshToken(7d)', fontsize=8)
    arrow(ax, RX, 5.54, RX, 5.06)
    rbox(RX, 4.8, '更新最后登录时间\n返回双Token+用户信息', fontsize=8)
    arrow(ax, RX, 4.54, RX, 4.06)
    term(RX, 3.8, '登录成功')

    # 登录失败分支（文字标注向右，不超出画布）
    for (fy, txt) in [(8.9, '用户不存在'), (7.86, '密码错误'), (6.82, '账号已封禁')]:
        ax.annotate('', xy=(10.5, fy), xytext=(RX+DW/2, fy),
                    arrowprops=dict(arrowstyle='->', color=G_ARR, lw=1.1), zorder=2)
        ax.text(RX+DW/2+0.08, fy+0.1, '否', ha='left', fontsize=8, color='#333', zorder=5)
        ax.text(10.55, fy, txt, ha='left', va='center', fontsize=8, color='#555', zorder=4)

    # 分隔线
    ax.plot([DIV, DIV], [3.6, 11.45], '--', color='#AAAAAA', lw=1, zorder=1)

    save(fig, '03_login_flow.png')

# ══════════════════════════════════════════════════════════════
# 4. JWT Token 刷新流程图
# ══════════════════════════════════════════════════════════════
def draw_token_flow():
    fig, ax = plt.subplots(figsize=(8, 11))
    ax.set_xlim(0, 8); ax.set_ylim(0, 11)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(4, 10.6, 'JWT 双令牌刷新流程图', ha='center', fontsize=12, weight='bold')

    def rbox(x, y, text, **kw): box(ax, x, y, 2.8, 0.55, text, **kw)
    def dmd(x, y, text): diamond(ax, x, y, 2.8, 0.6, text)
    def term(x, y, text):
        e = mpatches.Ellipse((x, y), 2.4, 0.5, fc=G_TERM_F, ec=G_TERM_E, lw=1.2, zorder=3)
        ax.add_patch(e)
        ax.text(x, y, text, ha='center', va='center', fontsize=9, weight='bold', zorder=4)

    term(4, 10.2, '前端发起 API 请求')
    arrow(ax, 4, 9.95, 4, 9.5)
    dmd(4, 9.2, 'AccessToken 剩余\n< 15 分钟?')
    # No → 直接发送
    arrow(ax, 5.4, 9.2, 6.8, 9.2, label='否')
    ax.text(7.0, 9.2, '直接发送请求', ha='left', va='center', fontsize=8, color='#555')
    # Yes
    arrow(ax, 4, 8.9, 4, 8.4, label='是')
    dmd(4, 8.1, 'isRefreshing\n== true?')
    # 已在刷新
    arrow(ax, 5.4, 8.1, 6.8, 8.1, label='是')
    rbox(6.8, 7.8, '加入 requestQueue\n等待刷新完成', fc=G_NOTE_F, ec=G_NOTE_E, fontsize=8)
    # 未在刷新
    arrow(ax, 4, 7.8, 4, 7.3, label='否')
    rbox(4, 7.05, '设置 isRefreshing=true')
    arrow(ax, 4, 6.77, 4, 6.3)
    rbox(4, 6.05, '调用 /auth/refresh\n携带 RefreshToken', fontsize=8)
    arrow(ax, 4, 5.77, 4, 5.3)
    dmd(4, 5.0, '刷新是否\n成功?')
    # 成功
    arrow(ax, 4, 4.7, 4, 4.2, label='是')
    rbox(4, 3.95, '更新本地 AccessToken\nRefreshToken')
    arrow(ax, 4, 3.67, 4, 3.2)
    rbox(4, 2.95, '设置 isRefreshing=false\n批量重发 requestQueue', fontsize=8)
    arrow(ax, 4, 2.67, 4, 2.2)
    term(4, 1.95, '所有请求继续执行')

    # 失败分支
    arrow(ax, 5.4, 5.0, 6.0, 5.0, label='否')
    rbox(7.1, 5.0, '清空 Token\n跳转登录页', fc=G_ERR_F, ec=G_ERR_E, fontsize=8)

    # requestQueue 重发连线
    ax.annotate('', xy=(5.4, 2.95), xytext=(6.8, 7.5),
                arrowprops=dict(arrowstyle='->', color='#666666', lw=1, linestyle='dashed',
                                connectionstyle='arc3,rad=-0.3'), zorder=2)
    ax.text(6.5, 5.4, '刷新完成后\n统一重发', ha='center', fontsize=7, color='#666666', style='italic')

    save(fig, '04_token_refresh.png')

# ══════════════════════════════════════════════════════════════
# 5. 资源上传审核流程图
# ══════════════════════════════════════════════════════════════
def draw_resource_flow():
    fig, ax = plt.subplots(figsize=(7, 12))
    ax.set_xlim(0, 7); ax.set_ylim(0, 12)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(3.5, 11.6, '资源上传审核流程图', ha='center', fontsize=12, weight='bold')

    def rbox(x, y, text, **kw): box(ax, x, y, 2.6, 0.55, text, **kw)
    def dmd(x, y, text): diamond(ax, x, y, 2.6, 0.58, text)
    def term(x, y, text):
        e = mpatches.Ellipse((x, y), 2.2, 0.5, fc=G_TERM_F, ec=G_TERM_E, lw=1.2, zorder=3)
        ax.add_patch(e)
        ax.text(x, y, text, ha='center', va='center', fontsize=9, weight='bold', zorder=4)

    # 用户侧
    ax.text(2.5, 11.2, '用 户 侧', ha='center', fontsize=10, weight='bold', color='#333333')
    term(2.5, 10.8, '用户选择文件上传')
    arrow(ax, 2.5, 10.55, 2.5, 10.1)
    dmd(2.5, 9.82, '文件格式 / 大小\n是否合规?')
    arrow(ax, 3.8, 9.82, 5.0, 9.82, label='否')
    rbox(5.7, 9.82, '提示格式或大小\n超限错误', fc=G_ERR_F, ec=G_ERR_E, fontsize=8)
    arrow(ax, 2.5, 9.53, 2.5, 9.1, label='是')
    rbox(2.5, 8.85, '请求后端获取\nOSS 上传签名', fontsize=8)
    arrow(ax, 2.5, 8.57, 2.5, 8.1)
    rbox(2.5, 7.85, '直接 PUT 文件至\n阿里云 OSS', fontsize=8)
    arrow(ax, 2.5, 7.57, 2.5, 7.1)
    rbox(2.5, 6.85, '提交资源元信息\n至后端入库', fontsize=8)
    arrow(ax, 2.5, 6.57, 2.5, 6.1)
    rbox(2.5, 5.85, '资源状态: 待审核(0)\n仅上传者可见', fc=G_NOTE_F, ec=G_NOTE_E, fontsize=8)

    # 管理员侧
    ax.text(2.5, 5.2, '管 理 员 侧', ha='center', fontsize=10, weight='bold', color='#333333')
    arrow(ax, 2.5, 5.57, 2.5, 5.1)
    rbox(2.5, 4.85, '管理员查看待审核\n资源列表（含预览）', fontsize=8)
    arrow(ax, 2.5, 4.57, 2.5, 4.1)
    dmd(2.5, 3.82, '审核\n结果?')

    # 通过
    arrow(ax, 1.2, 3.82, 0.2, 3.82)
    ax.text(0.2, 3.82, '通过', ha='right', va='center', fontsize=8)
    arrow(ax, 0.5, 3.82, 0.5, 2.8)
    rbox(0.5, 2.6, '状态→1(通过)\n资源公开可见', fc=G_OK_F, ec=G_OK_E, fontsize=8)
    arrow(ax, 0.5, 2.32, 0.5, 1.8)
    rbox(0.5, 1.6, '发放上传积分+10\n通知用户', fontsize=8)

    # 拒绝
    arrow(ax, 3.8, 3.82, 4.8, 3.82)
    ax.text(4.8, 3.82, '拒绝', ha='left', va='center', fontsize=8)
    arrow(ax, 5.1, 3.82, 5.1, 2.8)
    rbox(5.1, 2.6, '状态→2(拒绝)\n填写拒绝原因', fc=G_ERR_F, ec=G_ERR_E, fontsize=8)
    arrow(ax, 5.1, 2.32, 5.1, 1.8)
    rbox(5.1, 1.6, '推送通知至上传者\n用户可修改重新提交', fontsize=8)

    ax.plot([0, 7], [5.38, 5.38], '--', color='#AAAAAA', lw=1, zorder=1)

    save(fig, '05_resource_flow.png')

# ══════════════════════════════════════════════════════════════
# 6. 任务状态机流转图
# ══════════════════════════════════════════════════════════════
def draw_task_state():
    fig, ax = plt.subplots(figsize=(10, 7))
    ax.set_xlim(0, 10); ax.set_ylim(0, 7)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(5, 6.6, '任务大厅状态机流转图', ha='center', fontsize=12, weight='bold')

    states = {
        'PENDING':     (2,   5.0, '待接单\nPENDING(0)',   '#EEEEEE', '#555555'),
        'IN_PROGRESS': (5.5, 5.0, '进行中\nIN_PROGRESS(2)', '#E0E0E0', '#444444'),
        'SUBMITTED':   (8.5, 5.0, '待确认\nSUBMITTED(3)',  '#D8D8D8', '#666666'),
        'COMPLETED':   (8.5, 2.5, '已完成\nCOMPLETED(4)',  '#CCCCCC', '#333333'),
        'CANCELLED':   (5.5, 2.5, '已取消\nCANCELLED(5)',  '#C0C0C0', '#888888'),
        'EXPIRED':     (2,   2.5, '已超时\nEXPIRED(6)',    '#B8B8B8', '#777777'),
    }
    w, h = 2.2, 0.85
    centers = {}
    for k, (x, y, txt, fc, ec) in states.items():
        rect = FancyBboxPatch((x-w/2, y-h/2), w, h, boxstyle="round,pad=0.08",
                              fc=fc, ec=ec, lw=1.8, zorder=3)
        ax.add_patch(rect)
        ax.text(x, y, txt, ha='center', va='center', fontsize=9, weight='bold',
                color='#1A1A1A', zorder=4)
        centers[k] = (x, y)

    transitions = [
        ('PENDING',     'IN_PROGRESS', '接  单',       'right', 'left',  0),
        ('IN_PROGRESS', 'SUBMITTED',   '提交任务',      'right', 'left',  0),
        ('SUBMITTED',   'COMPLETED',   '发布者确认\n积分转移', 'bottom', 'top', 0),
        ('PENDING',     'CANCELLED',   '发布者取消',    'bottom', 'top',  0),
        ('IN_PROGRESS', 'CANCELLED',   '双方协商取消',  'bottom', 'top',  0),
        ('SUBMITTED',   'IN_PROGRESS', '发布者驳回',    'left',  'right', 0.3),
        ('PENDING',     'EXPIRED',     '截止时间到期\n自动超时', 'bottom', 'top', 0),
        ('IN_PROGRESS', 'EXPIRED',     '超时未提交\n自动超时',   'bottom', 'top', 0.3),
    ]

    side_offset = {'right':(1.1, 0), 'left':(-1.1, 0), 'top':(0, 0.43), 'bottom':(0, -0.43)}
    for (s, e, lbl, ss, es, rad) in transitions:
        x1, y1 = centers[s]; dx1, dy1 = side_offset[ss]
        x2, y2 = centers[e]; dx2, dy2 = side_offset[es]
        conn = f'arc3,rad={rad}' if rad else 'arc3,rad=0'
        color = '#444444' if e == 'COMPLETED' else ('#888888' if e in ('CANCELLED','EXPIRED') else '#555555')
        ax.annotate('', xy=(x2+dx2, y2+dy2), xytext=(x1+dx1, y1+dy1),
                    arrowprops=dict(arrowstyle='->', color=color, lw=1.5,
                                   connectionstyle=conn), zorder=2)
        mx, my = (x1+dx1+x2+dx2)/2, (y1+dy1+y2+dy2)/2
        ax.text(mx, my+0.15, lbl, ha='center', fontsize=7.5, color='#444', zorder=5,
                bbox=dict(fc='white', ec='none', pad=1))

    # 初始箭头
    ax.annotate('', xy=(0.9, 5.0), xytext=(0.1, 5.0),
                arrowprops=dict(arrowstyle='->', color='#555', lw=1.5), zorder=2)
    ax.text(0.1, 5.25, '发布任务', fontsize=8, color='#555')

    # 图例
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(fc=G_OK_F, ec=G_OK_E, label='正常完成'),
        Patch(fc=G_ERR_F, ec=G_ERR_E, label='已取消'),
        Patch(fc='#B8B8B8', ec='#777777', label='已超时'),
    ]
    ax.legend(handles=legend_elements, loc='lower right', fontsize=8.5, framealpha=0.9)
    ax.text(5, 0.4, '* 定时任务每小时扫描超时任务，自动将 PENDING/IN_PROGRESS 转为 EXPIRED',
            ha='center', fontsize=7.5, color='#888', style='italic')

    save(fig, '06_task_state.png')

# ══════════════════════════════════════════════════════════════
# 7. 时序图：资源 OSS 直传
# ══════════════════════════════════════════════════════════════
def draw_seq_oss():
    fig, ax = plt.subplots(figsize=(10, 9))
    ax.set_xlim(0, 10); ax.set_ylim(0, 9)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(5, 8.7, '资源 OSS 直传时序图', ha='center', fontsize=12, weight='bold')

    actors = [('用户浏览器', 1.5), ('前端 uni-app', 3.5), ('Spring Boot\n后端', 6.0), ('阿里云 OSS', 8.5)]
    colors = ['#4A4A4A', '#5E5E5E', '#717171', '#848484']

    for i, (name, x) in enumerate(actors):
        box(ax, x, 8.2, 1.6, 0.5, name, fc=colors[i], ec=colors[i], fontsize=9, bold=True)
        ax.plot([x, x], [0.3, 7.95], '--', color=colors[i], lw=1, alpha=0.5, zorder=1)

    def msg(x1, x2, y, text, dashed=False, back=False, label_above=True):
        style = '<-' if back else '->'
        ls = 'dashed' if dashed else 'solid'
        ax.annotate('', xy=(x2, y), xytext=(x1, y),
                    arrowprops=dict(arrowstyle=style, color='#333', lw=1.2,
                                   linestyle=ls), zorder=2)
        lx = (x1 + x2) / 2
        ly = y + 0.12 if label_above else y - 0.16
        ax.text(lx, ly, text, ha='center', fontsize=8, color='#1A1A1A', zorder=5)

    def act_bar(x, y, h, fc='#DEDEDE', ec='#666666'):
        rect = plt.Rectangle((x-0.25, y-h), 0.5, h, fc=fc, ec=ec, lw=1, zorder=3)
        ax.add_patch(rect)

    # 交互序列
    seqs = [
        (1.5, 3.5, 7.6, '选择文件，点击上传'),
        (3.5, 6.0, 7.0, 'GET /resource/oss-signature'),
        (6.0, 3.5, 6.5, '返回 Policy + Signature + Host', True, True),
        (3.5, 8.5, 6.0, 'PUT 文件 to OSS（携带签名）'),
        (8.5, 3.5, 5.5, '返回 200 + 文件 URL', True, True),
        (3.5, 6.0, 5.0, 'POST /resource/upload\n(title + url + meta)'),
        (6.0, 3.5, 4.4, '返回 rid + status=待审核', True, True),
        (3.5, 1.5, 3.9, '展示上传成功 + 待审核提示', False, True),
    ]
    for seq in seqs:
        x1, x2, y = seq[0], seq[1], seq[2]
        text = seq[3]
        dashed = seq[4] if len(seq) > 4 else False
        back = seq[5] if len(seq) > 5 else False
        msg(x1, x2, y, text, dashed=dashed, back=back)

    # 激活条
    for (x, y, h) in [(3.5, 7.95, 0.4), (6.0, 7.35, 0.3), (8.5, 6.3, 0.65), (6.0, 5.3, 0.45)]:
        act_bar(x, y, h)

    save(fig, '07_seq_oss.png')

# ══════════════════════════════════════════════════════════════
# 8. 时序图：Token 并发刷新
# ══════════════════════════════════════════════════════════════
def draw_seq_token():
    fig, ax = plt.subplots(figsize=(11, 9))
    ax.set_xlim(0, 11); ax.set_ylim(0, 9)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(5.5, 8.7, 'JWT Token 并发刷新时序图', ha='center', fontsize=12, weight='bold')

    actors = [('请求 A', 1.2), ('请求 B', 2.8), ('请求 C', 4.4),
              ('前端 request.ts', 6.5), ('后端 /auth/refresh', 9.5)]
    colors = ['#3A3A3A', '#505050', '#656565', '#7A7A7A', '#919191']

    for i, (name, x) in enumerate(actors):
        c = colors[i]
        box(ax, x, 8.2, 1.3, 0.5, name, fc=c, ec=c, fontsize=8.5, bold=True)
        ax.plot([x, x], [0.3, 7.95], '--', color=c, lw=1, alpha=0.4, zorder=1)

    def msg(x1, x2, y, text, color='#333', dashed=False, back=False):
        style = '<-' if back else '->'
        ls = 'dashed' if dashed else 'solid'
        ax.annotate('', xy=(x2, y), xytext=(x1, y),
                    arrowprops=dict(arrowstyle=style, color=color, lw=1.1, linestyle=ls), zorder=2)
        lx = (x1 + x2) / 2
        ax.text(lx, y + 0.11, text, ha='center', fontsize=7.5, color='#1A1A1A', zorder=5)

    def note(x, y, text, fc=None):
        if fc is None: fc = G_NOTE_F
        box(ax, x, y, 2.0, 0.38, text, fc=fc, ec=G_NOTE_E, fontsize=7.5)

    # 三个并发请求到达
    for (rx, y) in [(1.2, 7.4), (2.8, 7.15), (4.4, 6.9)]:
        msg(rx, 6.5, y, '发起 API 请求')

    note(6.5, 6.3, '检测 Token 剩余 < 15min')

    # 请求A 触发刷新
    msg(6.5, 9.5, 5.9, '请求A: POST /auth/refresh')
    note(6.5, 5.5, 'isRefreshing = true\n请求B/C 入队等待')

    # 请求B C 入队
    ax.annotate('', xy=(6.5, 5.1), xytext=(2.8, 6.85),
                arrowprops=dict(arrowstyle='->', color='#555555', lw=1, linestyle='dashed',
                                connectionstyle='arc3,rad=0.2'), zorder=2)
    ax.text(4.5, 6.0, '加入 requestQueue', ha='center', fontsize=7.5, color='#555555')
    ax.annotate('', xy=(6.5, 5.0), xytext=(4.4, 6.65),
                arrowprops=dict(arrowstyle='->', color='#666666', lw=1, linestyle='dashed',
                                connectionstyle='arc3,rad=0.15'), zorder=2)

    # 后端返回新Token
    msg(9.5, 6.5, 4.5, '返回新 AccessToken', color='#666666', dashed=True, back=True)
    note(6.5, 4.1, '更新本地 Token\nisRefreshing = false')

    # 批量重发
    for (rx, y) in [(1.2, 3.5), (2.8, 3.25), (4.4, 3.0)]:
        msg(6.5, rx, y, '携带新Token重发原请求', dashed=True)

    note(6.5, 2.6, '遍历 requestQueue\n批量 resolve')

    for (rx, y) in [(1.2, 2.0), (2.8, 1.75), (4.4, 1.5)]:
        ax.text(rx, y, '✓ 请求完成', ha='center', fontsize=8, color='#444444', weight='bold')

    save(fig, '08_seq_token.png')


# ══════════════════════════════════════════════════════════════
# 9. 系统总体架构图
# ══════════════════════════════════════════════════════════════
def draw_architecture():
    fig, ax = plt.subplots(figsize=(13, 8))
    ax.set_xlim(0, 13); ax.set_ylim(0, 8)
    ax.axis('off')
    ax.set_facecolor(G_BG)
    ax.text(6.5, 7.65, 'CampusLink 系统总体架构图', ha='center', fontsize=13, weight='bold')

    # 四层定义：(层名, y中心, 层高, 填充色, 边框色)
    layers = [
        ('客户端层',  6.3, 1.1, '#EEEEEE', '#666666'),
        ('接  入  层',  4.8, 0.7, '#E4E4E4', '#666666'),
        ('服务层',    3.1, 1.4, '#DCDCDC', '#555555'),
        ('数据层',    1.4, 1.4, '#D0D0D0', '#444444'),
    ]
    for (name, cy, lh, fc, ec) in layers:
        rect = FancyBboxPatch((0.3, cy - lh/2), 12.4, lh,
                              boxstyle="round,pad=0.05", fc=fc, ec=ec, lw=1.4, zorder=1)
        ax.add_patch(rect)
        ax.text(0.08, cy, name, ha='center', va='center', fontsize=8.5, weight='bold',
                color='#333', rotation=0, zorder=2)

    # ── 组件框：用单次 ax.text + \n 渲染多行，避免行间重叠 ──
    def comp(x, y, w, h, lines, fc=G_BOX_F, ec=G_BOX_E, fs=8):
        r = FancyBboxPatch((x - w/2, y - h/2), w, h,
                           boxstyle="round,pad=0.06", fc=fc, ec=ec, lw=1.2, zorder=3)
        ax.add_patch(r)
        ax.text(x, y, '\n'.join(lines), ha='center', va='center',
                fontsize=fs, linespacing=1.55, zorder=4)

    # 客户端层
    comp(3.0, 6.3, 2.8, 0.88, ['H5 用户端 (uni-app)', 'Vue 3 + TypeScript', 'Port: 5173'], fc='#F5F5F5', ec='#777777')
    comp(7.5, 6.3, 2.8, 0.88, ['管理后台 (Vue 3)', 'Element Plus + ECharts', 'Port: 5174'], fc='#F0F0F0', ec='#777777')
    comp(11.0, 6.3, 1.5, 0.88, ['移动端 H5', '(微信/浏览器)'], fc='#EBEBEB', ec='#888888')

    # 接入层
    comp(3.8, 4.8, 3.2, 0.52, ['Nginx 反向代理（生产）', 'HTTPS 终止 / 负载均衡'], fc='#EBEBEB', ec='#666666')
    comp(8.8, 4.8, 3.2, 0.52, ['Vite Proxy（开发）', '/api  →  localhost:8080'], fc='#EBEBEB', ec='#666666')

    # 服务层
    comp(2.5, 3.15, 3.2, 1.15, ['Spring Boot 3.4.0', '24 个 REST 控制器', 'context-path: /api/v1', 'Port: 8080'], fc='#E8E8E8', ec='#555555')
    comp(6.3, 3.15, 2.4, 1.15, ['WebSocket 实时通信', '私信推送 / 通知推送'], fc='#E0E0E0', ec='#666666')
    comp(9.5, 3.15, 2.5, 1.15, ['阿里云 OSS 对象存储', 'DeepSeek API', 'AI 助手功能'], fc='#DADADA', ec='#777777')

    # 数据层
    comp(2.5, 1.45, 2.8, 1.1, ['MySQL 8.0', '17 张核心业务表', '逻辑删除 + 自动填充'], fc='#D8D8D8', ec='#444444')
    comp(6.3, 1.45, 2.8, 1.1, ['Redis 7.0', '验证码 TTL 缓存', '高频查询缓存'], fc='#D0D0D0', ec='#555555')
    comp(10.0, 1.45, 2.4, 1.1, ['MyBatisPlus 持久层', 'JPA 实体共用', '双 ORM 并存'], fc='#CCCCCC', ec='#666666')

    # 垂直连接箭头（层间）
    for x in [3.0, 7.5]:
        arrow(ax, x, 5.86, x, 5.07, color=G_ARR)
    for x in [3.8, 8.8]:
        arrow(ax, x, 4.54, x, 3.73, color=G_ARR)
    for x in [2.5, 6.3]:
        arrow(ax, x, 2.73, x, 2.0, color=G_ARR)

    # 拦截器链标注（在服务层右侧用小字注明）
    note_x = 12.1
    for (ny, txt) in [(3.55, 'AdminAuthInterceptor  /admin/**'),
                      (3.15, 'OptionalJwtInterceptor  (游客可访问)'),
                      (2.75, 'JwtAuthInterceptor  (强制登录)')]:
        ax.text(note_x, ny, txt, ha='left', va='center',
                fontsize=6.5, color='#555', style='italic', zorder=5)
    ax.plot([11.7, 12.05], [3.73, 3.73], '-', color='#AAAAAA', lw=0.8, zorder=2)
    ax.text(11.88, 3.78, '拦截器链', ha='center', fontsize=6.5, color='#777', zorder=5)

    save(fig, '09_architecture.png')


# ══════════════════════════════════════════════════════════════
# 运行所有
# ══════════════════════════════════════════════════════════════
if __name__ == '__main__':
    print("生成论文图表...")
    draw_usecase()
    draw_er()
    draw_login_flow()
    draw_token_flow()
    draw_resource_flow()
    draw_task_state()
    draw_seq_oss()
    draw_seq_token()
    draw_architecture()
    print("全部完成！")
