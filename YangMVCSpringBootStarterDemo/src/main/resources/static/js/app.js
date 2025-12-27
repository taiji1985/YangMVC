/**
 * YangMVC Spring Boot Starter Demo - JavaScript
 */

// 工具函数
const Utils = {
    // 格式化日期
    formatDate: function(date) {
        if (!date) return '';
        const d = new Date(date);
        return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN');
    },

    // 显示消息
    showMessage: function(message, type = 'info') {
        const alertClass = {
            'success': 'alert-success',
            'error': 'alert-danger',
            'warning': 'alert-warning',
            'info': 'alert-info'
        }[type] || 'alert-info';

        const alertDiv = document.createElement('div');
        alertDiv.className = `alert ${alertClass}`;
        alertDiv.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 5px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 1000;
            animation: slideIn 0.3s ease-out;
        `;
        alertDiv.textContent = message;

        document.body.appendChild(alertDiv);

        setTimeout(() => {
            alertDiv.style.animation = 'slideOut 0.3s ease-out';
            setTimeout(() => alertDiv.remove(), 300);
        }, 3000);
    },

    // 发送请求
    request: function(url, options = {}) {
        return fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'error') {
                throw new Error(data.message);
            }
            return data;
        });
    }
};

// API调用示例
const API = {
    // 获取用户列表
    getUsers: function() {
        return Utils.request('/user/list');
    },

    // 获取用户详情
    getUser: function(id) {
        return Utils.request(`/user/detail?id=${id}`);
    },

    // 添加用户
    addUser: function(data) {
        return Utils.request('/user/save', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    // 更新用户
    updateUser: function(id, data) {
        return Utils.request('/user/update', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    // 删除用户
    deleteUser: function(id) {
        return Utils.request(`/user/delete?id=${id}`, {
            method: 'POST'
        });
    },

    // 获取统计信息
    getStats: function() {
        return Utils.request('/api/stats');
    }
};

// 表单处理
const Form = {
    // 获取表单数据
    getData: function(formElement) {
        const formData = new FormData(formElement);
        const data = {};
        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }
        return data;
    },

    // 设置表单数据
    setData: function(formElement, data) {
        for (let key in data) {
            const input = formElement.querySelector(`[name="${key}"]`);
            if (input) {
                input.value = data[key];
            }
        }
    },

    // 清空表单
    clear: function(formElement) {
        formElement.reset();
    }
};

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    console.log('YangMVC Spring Boot Starter Demo Loaded!');

    // 添加CSS动画
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }

        .alert {
            background: white;
            border-left: 4px solid #667eea;
            min-width: 300px;
        }

        .alert-success { border-left-color: #28a745; }
        .alert-error { border-left-color: #dc3545; }
        .alert-warning { border-left-color: #ffc107; }
        .alert-info { border-left-color: #17a2b8; }
    `;
    document.head.appendChild(style);
});

// 导出到全局
window.Utils = Utils;
window.API = API;
window.Form = Form;
