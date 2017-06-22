/*
 * Copyright (C) 2017 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mcmiddleearth.warp;

import io.github.mywarp.mywarp.warp.Warp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Eriol_Eandur
 */
public class MCMEWarpUtil {
    
    public static Warp getRandomWarp(Collection<Warp> warps) {
        List<Warp> valid = new ArrayList<>();
        for(Warp warp: warps) {
            if(Character.isUpperCase(warp.getName().charAt(0))) {
                valid.add(warp);
            }
        }
        if(valid.size()>0) {
            Collections.shuffle(valid);
            return valid.get(0);
        } else {
            return null;
        }
    }
}
