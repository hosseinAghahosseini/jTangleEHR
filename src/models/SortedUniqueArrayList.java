/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.ArrayList;

/**
 * A helper class to store visited nodes
 * @author hosseinAghahosseini
 */

public class SortedUniqueArrayList {
    
    private ArrayList<String> List;
    
    public SortedUniqueArrayList()
    {
        List = new ArrayList<>();
    }
    
    public boolean addLinear(String s)
    {
        if(List.isEmpty())
        {
            List.add(s);
            return true;
        }
        else if(List.size() == 1)
        {
            int compare = s.compareTo(List.get(0));
            if(compare > 0)
            {
                List.add(s);
                return true;
            }
            else if(compare == 0)
            {
                return false;
            }
            else{
                List.add(0, s);
                return true;
            }
        }
        else
        {
            for(int i = 0; i < List.size(); i++)
            {
                int compare = s.compareTo(List.get(i));

                if(i != List.size() - 1)
                {
                    if(compare == 0)
                    {
                        return false;
                    }
                    else if(compare < 0)
                    {
                        List.add(i, s);
                        return true;
                    }
                }
                else
                {
                    if(compare > 0)
                    {
                        List.add(s);
                        return true;
                    }
                    else if(compare == 0)
                    {
                        return false;
                    }
                    else{
                        List.add(i, s);
                        return true;
                    }
                }

            }
        }
        
        return false;
    }
    
    public int add(String s)
    {
        if(List.isEmpty())
        {
            List.add(s);
            return 0;
        }
        else if(List.size() == 1)
        {
            int compare = s.compareTo(List.get(0));
            if(compare > 0)
            {
                List.add(s);
                return 1;
            }
            else if(compare == 0)
            {
                return -1;
            }
            else{
                List.add(0, s);
                return 0;
            }
        }
        else
        {
            return addHelper(0, List.size() - 1, s);
        }
    }
    
    public boolean remove(String s)
    {
        if(!List.isEmpty())
        {
            for(int i = List.size() - 1; i >= 0; i--)
            {
                if(List.get(i).equals(s))
                {
                    List.remove(i);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean removeAt(int index)
    {
        if(index < 0) return false;
        if(List.isEmpty()) return false;
        if(index + 1 > List.size()) return false;
        
        List.remove(index);
        return true;
    }
    
    public String get(int index)
    {
        if(index < 0) return null;
        if(List.isEmpty()) return null;
        if(index + 1 > List.size()) return null;
        return List.get(index);
    }
    
    /**
     * Returns the corresponding index of the found string.
     * If not found, -1 will be returned
    */
    public int exists(String s)
    {
        return existHelper(0, List.size() - 1, s);
    }
    
    int existHelper(int startIndex, int endIndex, String s)
    {
        int index = ( startIndex + endIndex ) / 2;
        int compare = s.compareTo(List.get(index));
        if(compare == 0)
        {
            return index;
        }
        else if(startIndex == endIndex)
        {
            return -1;
        }
        else if(compare > 0)
        {
            return existHelper(index + 1, endIndex, s);
        }
        else
        {
            return existHelper(startIndex, index, s);
        }
    }
    
    int addHelper(int startIndex, int endIndex, String s)
    {
        int index = ( startIndex + endIndex ) / 2;
        int compare = s.compareTo(List.get(index));
        if(compare == 0)
        {
            return -1;
        }
        else if(startIndex == endIndex)
        {
            List.add(index, s);
            return index;
        }
        else if(compare > 0)
        {
            return addHelper(index + 1, endIndex, s);
        }
        else
        {
            return addHelper(startIndex, index, s);
        }
    }
    
    public ArrayList<String> getList()
    {
        return List;
    }
    
    public void setList(ArrayList<String> list)
    {
        List = list;
    }
}
