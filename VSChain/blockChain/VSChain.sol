// SPDX-License-Identifier: MIT
pragma solidity ^0.8.16;
contract firstwork{
    bool public isPass;
    mapping(bytes32 => bytes32) private IdHashRoot;
    mapping(bytes32 => bytes32) private updHash;
    function inputHashRoot(bytes32 id, bytes32 hashRoot) public {
        IdHashRoot[id] = hashRoot;
    }

    function setUpdHash(bytes32 updTau, bytes32 hash) public {
        updHash[updTau] = hash;
    }
    
    function verifyAndUpdHash(bytes32 keyword, bytes32 updTau, bytes calldata VO, uint[] memory updPathId, uint[] memory updId) public {
        bytes32 oldHash = computeOldHash(VO, updPathId);
        bytes32 thisHash = IdHashRoot[keyword];
        if(oldHash == thisHash && setHashXor(updId) == updHash[updTau]) {
            isPass = true;
            delete updHash[updTau];
            IdHashRoot[keyword] = computNewHash(VO, updPathId, updId);
        }else {
            isPass = false;
        }
    }

    function verifyAndUpdHash(bytes32 keyword, bytes calldata VO, uint[] memory updPathId, uint[] memory updId) public {
        bytes32 oldHash = computeOldHash(VO, updPathId);
        bytes32 thisHash = IdHashRoot[keyword];
        if(oldHash == thisHash) {
            isPass = true;
            IdHashRoot[keyword] = computNewHash(VO, updPathId, updId);
        }else {
            isPass = false;
        }
    }
    function computeOldHash(bytes calldata VO, uint[] memory updPathId) public returns(bytes32 res){//不包括end
        (res,,) = computeOldHashMain(VO, 2, updPathId, 0);
    }
   event ev2(bytes);
    function computeOldHashMain(bytes calldata VO, uint tag, uint[] memory updPathId, uint index) internal returns(bytes32 res, uint newTag, uint newIndex){//不包括end
        if(tag < VO.length) {
            bytes32 leftHash;
            bytes32 rightHash;
            bytes32 data;
            //分情况讨论获取左边哈希值
            if(VO[tag] == 0xff && VO[tag + 1] == 0xaa) {//<<...>,...>
                (leftHash, tag, index) = computeOldHashMain(VO, tag + 2, updPathId, index);
            } else if(VO[tag] == 0xff && VO[tag + 1] == 0xbb){//<|...,...|,...>
                tag = breakUpdBraket(VO, tag + 2);
            } else if (VO[tag] == 0x00 && VO[tag + 1] == 0x00){//<,...> 左哈希为空，不需要操作
            } else {//<_,_,...>
                leftHash = bytes32(VO[tag: tag + 32]);
                tag += 32;
            }
            data = keccak256(abi.encodePacked(updPathId[index]));
            index++;
            //接下来为逗号
            tag += 2;
            if (VO[tag] == 0xff && VO[tag + 1] == 0xaa) {//<...,<>>
                (rightHash, tag, index) = computeOldHashMain(VO, tag + 2, updPathId, index);
            } else if(VO[tag] == 0xff && VO[tag + 1] == 0xbb){//<...,|_,...,...|>
                tag = breakUpdBraket(VO, tag + 2);
            } else if (VO[tag] == 0xaa && VO[tag + 1] == 0xff){//<...,> 右哈希为空，不需要操作
            } else {//<...,_>
                rightHash = bytes32(VO[tag:tag + 32]);
                tag += 32;
            }
            tag += 2;
            emit ev2(abi.encodePacked(data, leftHash, rightHash));
            res = keccak256(abi.encodePacked(data, leftHash, rightHash));
            newTag = tag;
        }
    }

    function breakUpdBraket(bytes calldata VO, uint tag) internal pure returns(uint newTag) {
        uint lBraket = 1;
        while (lBraket != 0) {
            if(VO[tag] == 0x00 && VO[tag + 1] == 0x00) {
                tag += 2;
            }else if(VO[tag] == 0xff && VO[tag + 1] == 0xbb) {
                lBraket++;
                tag += 2;
            }else if(VO[tag] == 0xbb && VO[tag + 1] == 0xff) {
                lBraket--;
                tag += 2;
            }else {
                tag += 32;
            }
        }
        newTag = tag;
    }

    struct util{
        uint[] updIdPath;
        uint[] updId;
        uint tag;
        uint pathIndex;
        uint idIndex;
    }

    function computNewHash(bytes calldata VO, uint[] memory updIdPath, uint[] memory updId) internal pure returns(bytes32 res){//不包括end
        util memory tool;
        tool.updIdPath = updIdPath;
        tool.updId = updId;
        tool.tag = 0;
        tool.pathIndex = 0;
        tool.idIndex = 0;
        res = computNewHashMain(VO, tool);
    }

    function computNewHashMain(bytes calldata VO, util memory tool) internal pure returns(bytes32 res){//不包括end
        bytes32 data;
        bytes32 leftHash;
        bytes32 rightHash;
        uint s = tool.tag;
        tool.tag += 2;
        //分情况讨论获取左边哈希值
        if(VO[tool.tag] == 0xff && (VO[tool.tag + 1] == 0xaa || VO[tool.tag + 1] == 0xbb)) {//<<...>,...>
            leftHash = computNewHashMain(VO, tool);
        } else if (VO[tool.tag] == 0x00 && VO[tool.tag + 1] == 0x00){//<,...> 左哈希为空，不需要操作
        } else {//<_,...>
            leftHash = bytes32(VO[tool.tag : tool.tag + 32]);
            tool.tag += 32;
        }
        // 初始时为<或者|
        if (VO[s] == 0xff && VO[s + 1] == 0xbb) {
            data = keccak256(abi.encodePacked(tool.updId[tool.idIndex]));
            tool.idIndex++;
        } else {
            data = keccak256(abi.encodePacked(tool.updIdPath[tool.pathIndex]));
            tool.pathIndex++;
        }
        //接下来为逗号
        tool.tag += 2;
        if (VO[tool.tag] == 0xff && (VO[tool.tag + 1] == 0xaa || VO[tool.tag + 1] == 0xbb)) {//<...,<>>
            rightHash = computNewHashMain(VO, tool);
        } else if ((VO[tool.tag] == 0xaa || VO[tool.tag] == 0xbb) && VO[tool.tag + 1] == 0xff){//<...,> 右哈希为空，不需要操作
        } else {//<...,_>
            rightHash = bytes32(VO[tool.tag: tool.tag + 32]);
            tool.tag += 32;
        }
        tool.tag += 2;
        res = keccak256(abi.encodePacked(data, leftHash, rightHash));
    }

    function computRootHash(int[] memory nodeId) public pure returns(bytes32 res){//包括high
        return computRootHashMain(nodeId, 0, int(nodeId.length - 1));
    }

    function computRootHashMain(int[] memory nodeId, int low, int high) internal pure returns(bytes32 res){//包括high
        if(nodeId.length == 0 || low > high){
            return res;
        }
        int mid = (low + high) / 2;
        res = keccak256(abi.encodePacked(nodeId[uint(mid)]));
        bytes32 left;
        left = computRootHashMain(nodeId, low, mid - 1);
        bytes32 right = computRootHashMain(nodeId, mid + 1, high);
        return keccak256(abi.encodePacked(res,left,right));
    }

    struct tools{
        bytes LBracket;
        bytes RBracket;
        bytes LUpdBracket;
        bytes RUpdBracket;
        bytes lable;
        int[] nodeId;
        int[] updId;
    }

    function getUpdVO(int[] calldata nodeId, int[] calldata updId) pure public returns(bytes memory res, int[] memory updPathId){//包括nR,uR
        tools memory tool;
        tool.LBracket = new bytes(2);
        tool.RBracket = new bytes(2);
        tool.LUpdBracket = new bytes(2);
        tool.RUpdBracket = new bytes(2);
        tool.lable = new bytes(2);
        tool.LBracket[0] = 0xff;
        tool.LBracket[1] = 0xaa;
        tool.RBracket[1] = 0xff;
        tool.RBracket[0] = 0xaa;
        tool.lable[0] = 0x00;
        tool.lable[1] = 0x00;
        tool.LUpdBracket[0] = 0xff;
        tool.LUpdBracket[1] = 0xbb;
        tool.RUpdBracket[1] = 0xff;
        tool.RUpdBracket[0] = 0xbb;
        tool.nodeId = nodeId;
        tool.updId = updId;
        int nL = 0;
        int uL = 0;
        int nR = int(nodeId.length - 1);
        int uR = int(updId.length - 1);
        updPathId = new int[](100);
        (res, ) = getUpdVOMain(nL, nR, uL, uR, tool, updPathId, 0);
    }



//---------------------------------------------------tools-----------------------------------------------------------
    
    function getUpdVOMain(int nL, int nR, int uL, int uR, tools memory tool, int[] memory updPathId, uint index) internal pure returns(bytes memory res, uint newIndex){
        if(uL <= uR){//有更新
            bytes memory leftRes;
            bytes memory rightRes;
            int mid;
            if(nL <= nR){//有节点
                mid = (nL + nR) / 2;
                int tag = binerySearch(tool.updId, tool.nodeId[uint(mid)], uL, uR);
                (leftRes, index) = getUpdVOMain(nL, mid - 1, uL, tag - 1, tool, updPathId, index);
                updPathId[index] = tool.nodeId[uint(mid)];
                index++;
                (rightRes, index) = getUpdVOMain(mid + 1, nR, tag, uR, tool, updPathId, index);
                res = abi.encodePacked(res, tool.LBracket, leftRes, tool.lable, rightRes, tool.RBracket);
            } else {//无节点
                mid = (uL + uR) / 2;
                (leftRes, ) = getUpdVOMain(nL, nR, uL, mid - 1, tool, updPathId, index);
                (rightRes, ) = getUpdVOMain(nL, nR, mid + 1, uR, tool, updPathId, index);
                res = abi.encodePacked(res, tool.LUpdBracket, leftRes, tool.lable, rightRes, tool.RUpdBracket);
            }
        }else{//无更新
            if(nL <= nR){//有节点
                res = abi.encodePacked(res,computRootHashMain(tool.nodeId, nL, nR));
            }else{//无节点
            }
        }
        newIndex = index;
    }

    function binerySearch(int[] memory ids, int target, int low, int high) internal pure returns(int){//包括high
        while(low < high){
            int mid = (low + high) / 2;
            if(target > ids[uint(mid)]){
                low = mid + 1;
            }else{
                high = mid;
            }
        }
        if(target > ids[uint(low)]){
            return low + 1;
        }
        return low;
    }

    function setHashXor(uint[] memory updId) public pure returns(bytes32){
        bytes memory r = abi.encodePacked(keccak256(abi.encodePacked(updId[0])));
        for (uint i = 1; i < updId.length; i++) {
            bytes memory t = new bytes(32);
            for (uint j = 0; j < 32; j++) {
                r[j] ^= t[j];
            }
        }
        return bytes32(r);
    }
}