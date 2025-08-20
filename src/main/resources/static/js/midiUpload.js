async function uploadMidi(action) {
    const fileInput = document.getElementById("file");
    const nameInput = document.getElementById("name");
    const descInput = document.getElementById("description");

    if (!fileInput.files.length) {
        alert("请选择一个 MIDI 文件！");
        return;
    }

    if (!nameInput.value.trim()) {
        alert("请输入歌曲名称！");
        return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("name", nameInput.value.trim());
    formData.append("description", descInput.value.trim());

    const url = "/midi/" + action; // /midi/save

    try {
        const res = await fetch(url, {
            method: "POST",
            body: formData
        });

        if (!res.ok) {
            throw new Error("HTTP " + res.status);
        }

        const result = await res.json();   // ✅ 先拿到 {success,message,data}

        if (result.success) {
            const song = result.data;

            // 更新上传结果提示
            document.getElementById("result").innerText = "✅ 上传成功: " + song.name;

            // 更新主页卡片里的信息
            document.getElementById("uploadInfo").innerHTML = `
                <strong>🎶 最近上传：</strong><br>
                <b>歌曲名：</b> ${song.name}<br>
                <b>简介：</b> ${song.description || "无"}<br>
                <b>乐器数量：</b> ${song.instrumentCount}
            `;

            // 关闭模态框
            closeUploadModal();

            // 同时把新文件加到右侧列表
            if (typeof files !== "undefined") {
                files.push({
                    id: song.id || Date.now(),
                    name: song.name,
                    desc: song.description
                });
                renderFileList();
            }
        } else {
            document.getElementById("result").innerText = "❌ 上传失败: " + result.message;
        }

    } catch (err) {
        document.getElementById("result").innerText = "❌ 请求失败: " + err;
    }
}
